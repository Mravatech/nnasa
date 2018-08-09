package com.mnassa.screen.group.profile

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import androidx.view.doOnLayout
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.clans.fab.FloatingActionButton
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.create.CreateEventController
import com.mnassa.screen.group.create.CreateGroupController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.invite.GroupInviteConnectionsController
import com.mnassa.screen.group.profile.events.GroupEventsController
import com.mnassa.screen.group.profile.posts.GroupPostsController
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_group_profile.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import kotlin.math.roundToInt

/**
 * Created by Peter on 5/14/2018.
 */
class GroupProfileController(args: Bundle) : MnassaControllerImpl<GroupProfileViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_profile
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    private var groupModel: GroupModel = args.getSerializable(EXTRA_GROUP) as GroupModel

    override val viewModel: GroupProfileViewModel by instance(arg = groupId)
    private val dialogHelper: DialogHelper by instance()

    private val viewPagerAdapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    HomePage.NEEDS.ordinal -> GroupPostsController.newInstance(groupId)
                    HomePage.EVENTS.ordinal -> GroupEventsController.newInstance(groupId)
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page).tag(formatTabControllerTag(position)))
            }
        }

        override fun getCount(): Int = HomePage.values().size

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            HomePage.NEEDS.ordinal -> fromDictionary(R.string.tab_home_posts_title)
            HomePage.EVENTS.ordinal -> fromDictionary(R.string.tab_home_events_title)
            else -> throw IllegalArgumentException("Invalid page position $position")
        }
    }

    // Collapsing avatar logic
    private val appBarStateChangeListener: AppBarStateChangeListener = object : AppBarStateChangeListener() {
        override fun onOffsetChanged(state: AppBarStateChangeListener.State, offset: Float) {
            translationView(offset)
        }
    }
    private val avatarSrcPoint = IntArray(2)
    private val avatarDestPoint = IntArray(2)
    private val expandedAvatarSizePx: Int
        get() = applicationContext!!.resources.getDimensionPixelSize(R.dimen.group_profile_avatar_expanded)
    private val collapsedAvatarSizePx: Int
        get() = applicationContext!!.resources.getDimensionPixelSize(R.dimen.group_profile_avatar_collapsed)
    private val avatarSizeDiff: Float
        get() = (expandedAvatarSizePx - collapsedAvatarSizePx).toFloat()

    private fun setupCollapsingToolbar(view: View) {
        view.app_bar.addOnOffsetChangedListener(appBarStateChangeListener)
        view.ivGroupAvatar.doOnLayout {
            resetPoints()
        }
    }

    private fun translationView(offset: Float) {
        val view = view ?: return
        val newAvatarSize = expandedAvatarSizePx - (avatarSizeDiff) * offset

        val expandAvatarSize = expandedAvatarSizePx.toFloat()
        val xAvatarOffset = (avatarDestPoint[0] - avatarSrcPoint[0] - (expandAvatarSize - newAvatarSize) / 2f) * offset
        // If avatar center in vertical, just half `(expandAvatarSize - newAvatarSize)`
        val yAvatarOffset = (avatarDestPoint[1] - avatarSrcPoint[1] - (expandAvatarSize - newAvatarSize)) * offset
        with(view) {
            val lp = ivGroupAvatar.layoutParams
            lp.width = newAvatarSize.roundToInt()
            lp.height = newAvatarSize.roundToInt()
            ivGroupAvatar.layoutParams = lp
            ivGroupAvatar.translationX = xAvatarOffset
            ivGroupAvatar.translationY = yAvatarOffset

            tvGroupDescription.alpha = (1 - offset) * 0.4f
            ivGroupInfo.alpha = 1 - offset
        }
    }

    private fun resetPoints() {
        val view = view ?: return
        val offset = appBarStateChangeListener.currentOffset

        val newAvatarSize = expandedAvatarSizePx - (avatarSizeDiff) * offset
        val expandAvatarSize = expandedAvatarSizePx.toFloat()

        with(view) {
            val tmpAvatarPoint = IntArray(2)
            ivGroupAvatar.getLocationOnScreen(tmpAvatarPoint)
            avatarSrcPoint[0] = (tmpAvatarPoint[0] - ivGroupAvatar.translationX -
                    (expandAvatarSize - newAvatarSize) / 2f).roundToInt()
            // If avatar center in vertical, just half `(expandAvatarSize - newAvatarSize)`
            avatarSrcPoint[1] = (tmpAvatarPoint[1] - ivGroupAvatar.translationY -
                    (expandAvatarSize - newAvatarSize)).roundToInt()

            space.getLocationOnScreen(avatarDestPoint)
        }

        view.post { translationView(offset) }
    }
    ///

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        appBarStateChangeListener.reset()

        with(view) {
            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
            val titleColor = ContextCompat.getColor(context, R.color.white)
            collapsingToolbarLayout.setCollapsedTitleTextColor(titleColor)
            collapsingToolbarLayout.setExpandedTitleColor(titleColor)

            vpGroupProfile.adapter = viewPagerAdapter
            tabLayout.setupWithViewPager(vpGroupProfile)
        }
        setupCollapsingToolbar(view)

        launchCoroutineUI { viewModel.groupChannel.consumeEach { bindGroup(it, view) } }
        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }

        initFab(view)
        bindGroup(groupModel, view)
    }

    private fun initFab(view: View) {
        with(view) {
            fabGroup.setClosedOnTouchOutside(true)
        }

        launchCoroutineUI {
            viewModel.groupPermissionsChannel.consumeEach { (_, canCreateEvent, canCreateGeneralPost, canCreateNeedPost, canCreateOfferPost) ->
                with(getViewSuspend()) {
                    fabGroup.removeAllMenuButtons()

                    if (canCreateGeneralPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_general_post))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateGeneralPostController.newInstance(group = groupModel))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    if (canCreateEvent) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_event))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateEventController.newInstance(group = groupModel))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    if (canCreateOfferPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_offer))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateOfferController.newInstance(group = groupModel))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    if (canCreateNeedPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_need))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateNeedController.newInstance(group = groupModel))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    fabGroup.isGone = !(
                            canCreateNeedPost ||
                                    canCreateOfferPost ||
                                    canCreateEvent ||
                                    canCreateGeneralPost)
                }
            }
        }
    }

    private fun initToolbar(view: View) {
        with(view) {
            toolbar.menu?.clear()
            if (groupModel.isAdmin) {
                toolbar.inflateMenu(R.menu.group_edit)
            } else {
                toolbar.inflateMenu(R.menu.group_view)
            }

            toolbar.menu.apply {
                findItem(R.id.action_group_edit)?.title = fromDictionary(R.string.group_menu_edit)
                findItem(R.id.action_group_details)?.title = fromDictionary(R.string.group_menu_details)
                findItem(R.id.action_invite_members)?.title = fromDictionary(R.string.group_menu_invite)
                findItem(R.id.action_group_leave)?.title = fromDictionary(R.string.group_menu_leave)
                findItem(R.id.action_group_delete)?.title = fromDictionary(R.string.group_delete_menu)
                findItem(R.id.action_group_wallet)?.title = fromDictionary(R.string.group_wallet_menu)
            }

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_group_edit -> open(CreateGroupController.newInstance(groupModel))
                    R.id.action_group_details -> open(GroupDetailsController.newInstance(groupModel))
                    R.id.action_invite_members -> open(GroupInviteConnectionsController.newInstance(groupModel))
                    R.id.action_group_leave -> viewModel.leave()
                    R.id.action_group_delete -> {
                        dialogHelper.showYesNoDialog(
                                context,
                                fromDictionary(R.string.group_delete_dialog),
                                onOkClick = { viewModel.delete() }
                        )
                    }
                    R.id.action_group_wallet -> open(WalletController.newInstanceGroup(groupModel))
                }
                true
            }
        }
    }

    private fun View.inflateMenuButton(text: String): FloatingActionButton {
        val button = FloatingActionButton(context)
        button.buttonSize = FloatingActionButton.SIZE_MINI
        button.setImageResource(R.drawable.ic_edit_white_24dp)
        button.colorNormal = ContextCompat.getColor(context, R.color.accent)
        button.colorPressed = ContextCompat.getColor(context, R.color.tealish)
        button.labelText = text

        return button
    }

    private fun bindGroup(group: GroupModel, view: View) {
        this.groupModel = group

        with(view) {
            val avatar = group.avatar
            ivGroupAvatar.avatarRound(avatar)
            if (avatar != null && !avatar.isBlank()) {
                ivGroupAvatar.setOnClickListener {
                    PhotoPagerActivity.start(view.context, listOf(avatar), 0)
                }
            }

            collapsingToolbarLayout.title = "  " + group.formattedName
            tvGroupDescription.text = group.formattedRole
            ivGroupInfo.setOnClickListener { open(GroupDetailsController.newInstance(group)) }
            tvGroupDescription.setOnClickListener { open(GroupDetailsController.newInstance(group)) }

            initToolbar(view)
        }
    }

    private enum class HomePage {
        NEEDS, EVENTS
    }

    private fun formatTabControllerTag(position: Int): String = "home_tab_controller_$position"

    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance(group: GroupModel): GroupProfileController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupProfileController(args)
        }
    }
}