package com.mnassa.screen.group.profile

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.create.CreateGroupController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.invite.GroupInviteConnectionsController
import com.mnassa.screen.group.members.GroupMembersController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_group_profile.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupProfileController(args: Bundle) : MnassaControllerImpl<GroupProfileViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_profile
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    private var groupModel: GroupModel = args.getSerializable(EXTRA_GROUP) as GroupModel

    override val viewModel: GroupProfileViewModel by instance(arg = groupId)
    private val adapter = PostsRVAdapter(withHeader = false)
    private val tagsAdapter = PostTagRVAdapter()
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val dialogHelper: DialogHelper by instance()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        adapter.onAttachedToWindow = { post -> viewModel.onAttachedToWindow(post) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = {
            controllerSubscriptionContainer.launchCoroutineUI {
                if (viewModel.groupPermissionsChannel.consume { receive() }.canCreateNeedPost) {
                    open(CreateNeedController.newInstance(group = groupModel))
                }
            }
        }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onPostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onHideInfoPostClickListener = viewModel::hideInfoPost
        adapter.onMoreItemClickListener = this::showPostMenu
        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.getNewsFeedChannel().subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().rlEmptyView }
            )
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
            llGroupMembers.setOnClickListener { open(GroupMembersController.newInstance(groupModel)) }
            flInfoSection.setOnClickListener { open(GroupDetailsController.newInstance(groupModel)) }
            llGroupInvites.setOnClickListener { if (groupModel.isAdmin) open(GroupInviteConnectionsController.newInstance(groupModel)) }
            rvGroupTags.adapter = tagsAdapter
            rvGroupPosts.adapter = adapter
        }

        launchCoroutineUI { viewModel.groupChannel.consumeEach { bindGroup(it, view) } }
        launchCoroutineUI { viewModel.tagsChannel.consumeEach { bindTags(it, view) } }
        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }
        
        initFab(view)
        bindGroup(groupModel, view)
    }

    override fun onDestroyView(view: View) {
        view.rvGroupTags.adapter = null
        view.rvGroupPosts.adapter = null
        super.onDestroyView(view)
    }

    private fun initFab(view: View) {
        with(view) {
            fabGroup.setClosedOnTouchOutside(true)
        }

        launchCoroutineUI {
            viewModel.groupPermissionsChannel.consumeEach { (_, _, canCreateGeneralPost, canCreateNeedPost, canCreateOfferPost) ->
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

                    //TODO: uncomment for events in groups
//                    if (permission.canCreateEvent) {
//                        button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_event))
//                        button.setOnClickListener {
//                            fabGroup.close(false)
//                            open(CreateEventController.newInstance(group = groupModel))
//                        }
//                        fabGroup.addMenuButton(button)
//                    }

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
//                                    permission.canCreateEvent || //TODO: uncomment for events in groups
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
        adapter.showMoreOptions = group.isAdmin

        with(view) {
            val avatar = group.avatar
            ivGroupAvatar.avatarRound(avatar)
            if (avatar != null && !avatar.isBlank()) {
                ivGroupAvatar.setOnClickListener {
                    PhotoPagerActivity.start(view.context, listOf(avatar), 0)
                }
            }

            tvGroupTitle.text = group.formattedName
            tvGroupSubTitle.text = group.formattedRole
            toolbar.title = tvGroupTitle.text

            tvMembersCount.text = group.numberOfParticipants.toString()
            tvInvitesCount.text = group.numberOfInvites.toString()
            initToolbar(view)
        }
    }

    private fun bindTags(tags: List<TagModel>, view: View) {
        tagsAdapter.set(tags)
        view.rvGroupTags.isGone = tags.isEmpty()
    }

    private fun showPostMenu(post: PostModel, view: View) {
        popupMenuHelper.showGroupPostItemMenu(view,
                onRemove = { viewModel.removePost(post) }
        )
    }

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