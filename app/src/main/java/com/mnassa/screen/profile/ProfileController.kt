package com.mnassa.screen.profile

import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.View
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.isGone
import com.mnassa.extensions.isMyProfile
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.group.profile.GroupProfileController
import com.mnassa.screen.group.select.SelectGroupController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.profile.create.RecommendUserController
import com.mnassa.screen.profile.common.*
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController(data: Bundle) : MnassaControllerImpl<ProfileViewModel>(data),
        ComplaintOtherController.OnComplaintResult,
        SelectGroupController.OnGroupSelectedListener,
        View.OnClickListener {

    override val layoutId: Int = R.layout.controller_profile
    private val accountId: String by lazy { args.getString(EXTRA_ACCOUNT_ID) }
    override val viewModel: ProfileViewModel by instance(arg = accountId)

    private var adapter = PostsRVAdapter(withHeader = false)
    private val dialog: DialogHelper by instance()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        adapter.isLoadingEnabled = savedInstanceState == null

        adapter.onDataChangedListener = { itemsCount ->
            view?.findViewById<View>(R.id.rlEmptyView)?.isGone = !adapter.dataStorage.isEmpty()
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.postChannel.consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.addAll(it.item)
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.isLoadingEnabled = true
                        adapter.dataStorage.clear()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.rvProfile.adapter = adapter
        view.toolbarProfile.setNavigationOnClickListener { close() }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }


        adapter.onCreateNeedClickListener = { open(CreateNeedController.newInstance()) }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onGroupClickListener = { open(GroupProfileController.newInstance(it)) }

        launchCoroutineUI {
            viewModel.statusesConnectionsChannel.consumeEach { connectionStatus ->
                val profile = viewModel.profileChannel.consume { receive() }
                if (!profile.isMyProfile) {
                    handleConnectionStatus(connectionStatus, view)
                }
            }
        }

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }

        launchCoroutineUI { viewModel.profileChannel.consumeEach { bindHeader() } }
        launchCoroutineUI { viewModel.statusesConnectionsChannel.consumeEach { bindHeader() } }
        launchCoroutineUI { viewModel.offersChannel.consumeEach { bindHeader() } }
        launchCoroutineUI { viewModel.interestsChannel.consumeEach { bindHeader() } }
    }

    private suspend fun bindHeader() {
        bindHeader(
                profile = viewModel.profileChannel.consume { receive() },
                offers = viewModel.offersChannel.consume { receive() },
                interests = viewModel.interestsChannel.consume { receive() },
                connectionStatus = viewModel.statusesConnectionsChannel.consume { receive() }
        )
    }


    private fun bindHeader(profile: ProfileAccountModel, offers: List<TagModel>, interests: List<TagModel>, connectionStatus: ConnectionStatus) {
        val view = view ?: return
        val parent = view?.flSecondHeader ?: return

        val viewHolder = when {
            parent.tag is BaseProfileHolder -> parent.tag as BaseProfileHolder
            profile.isMyProfile && profile.accountType == AccountType.ORGANIZATION -> CompanyProfileViewHolder.newInstance(parent, this, profile)
            profile.isMyProfile && profile.accountType == AccountType.PERSONAL -> PersonalProfileViewHolder.newInstance(parent, this, profile)
            !profile.isMyProfile && profile.accountType == AccountType.ORGANIZATION -> AnotherCompanyProfileHolder.newInstance(parent, this, profile)
            !profile.isMyProfile && profile.accountType == AccountType.PERSONAL -> AnotherPersonalProfileHolder.newInstance(parent, this, profile)
            else -> throw IllegalArgumentException("Wrong account type!")
        }
        if (parent.tag !is BaseProfileHolder) {
            parent.addView(viewHolder.itemView)
        }
        parent.tag = viewHolder

        viewHolder.bindProfile(profile)
        viewHolder.bindOffers(offers)
        viewHolder.bindInterests(interests)
        viewHolder.bindConnectionStatus(connectionStatus)

        //

        profile.avatar?.let { avatar ->
            view.ivCropImage.avatarSquare(avatar)
            view.ivCropImage.setOnClickListener {
                PhotoPagerActivity.start(it.context, listOf(avatar), 0)
            }
        }

        handleCollapsingToolbar(view, connectionStatus, profile)
        setTitle(profile, view)
        onEditProfile(profile, view)
        if (!profile.isMyProfile) {
            handleConnectionStatus(connectionStatus, view)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvPointsGiven -> open(WalletController.newInstance())
            R.id.tvProfileConnections -> open(AllConnectionsController.newInstance())
            R.id.tvConnectionStatus -> {
                launchCoroutineUI {
                    val connectionStatus = viewModel.statusesConnectionsChannel.consume { receive() }
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> dialog.yesNoDialog(view.context, fromDictionary(R.string.user_profile_you_want_to_disconnect)) {
                            viewModel.sendConnectionStatus(connectionStatus, accountId)
                        }
                        ConnectionStatus.SENT, ConnectionStatus.RECOMMENDED, ConnectionStatus.REQUESTED ->
                            viewModel.sendConnectionStatus(connectionStatus, accountId)
                    }
                }

            }

        }
    }

    private fun handleCollapsingToolbar(view: View, connectionStatus: ConnectionStatus, profileModel: ProfileAccountModel) {

        view.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val shouldShowFab = true

            val offset = view.resources.getDimensionPixelSize(R.dimen.profile_main_image_height)

            if (appBarLayout.totalScrollRange - Math.abs(verticalOffset) < offset) {
                if (!profileModel.isMyProfile && shouldShowFab) {
                    view.fabProfile.hide()
                }
            } else {
                if (!profileModel.isMyProfile && shouldShowFab) {
                    view.fabProfile.show()
                }
            }
        }
    }

    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(accountId, OTHER, value)
        }

    override fun onDestroyView(view: View) {
        view.rvProfile.adapter = null
        super.onDestroyView(view)
    }

    private fun handleConnectionStatus(connectionStatus: ConnectionStatus, view: View) {
        val fab = view.fabProfile
        fab.visibility = View.VISIBLE
        fab.setOnClickListener {
            launchCoroutineUI {
                viewModel.profileChannel.consume { receive() }.apply { open(ChatMessageController.newInstance(this)) }
            }
        }
        fab.setImageResource(R.drawable.ic_chat)

        when (connectionStatus) {
            ConnectionStatus.REQUESTED, ConnectionStatus.RECOMMENDED -> {
                view.rlConnectContainer.visibility = View.VISIBLE
                view.btnConnectUser.setOnClickListener {
                    viewModel.sendConnectionStatus(connectionStatus, accountId)
                }
            }
            else -> {
                view.rlConnectContainer.visibility = View.GONE
                view.btnConnectUser.setOnClickListener(null)
            }
        }
    }

    private fun onSettingsClick(profileModel: ProfileAccountModel, view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.user_profile_item, popup.menu)
        popup.menu.findItem(R.id.action_share_profile).title = "Share Profile" //todo set from dict
        popup.menu.findItem(R.id.action_complain_about_profile).title = "Complain about Profile" //todo set from dict
        popup.menu.findItem(R.id.action_invite_to_group_profile).title = fromDictionary(R.string.group_invite_profile_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share_profile -> open(RecommendUserController.newInstance(profileModel))
                R.id.action_complain_about_profile -> complainAboutProfile(profileModel, view)
                R.id.action_invite_to_group_profile -> open(SelectGroupController.newInstance(this, onlyAdmin = true))
            }
            true
        }
        popup.show()
    }

    override fun onGroupSelected(group: GroupModel) {
        viewModel.inviteToGroup(group)
    }

    private fun complainAboutProfile(profileModel: ProfileAccountModel, view: View) {
        launchCoroutineUI {
            val reportsList = viewModel.retrieveComplaints()
            dialog.showComplaintDialog(view.context, reportsList) {
                if (it.id == OTHER) {
                    val controller = ComplaintOtherController.newInstance()
                    controller.targetController = this@ProfileController
                    open(controller)
                } else {
                    viewModel.sendComplaint(profileModel.id, it.id, null)
                }
            }
        }
    }

    private fun onEditProfile(profileModel: ProfileAccountModel, view: View) {
        if (profileModel.isMyProfile) {
            view.ivProfileEdit.visibility = View.VISIBLE
            view.ivProfileEdit.setOnClickListener {
                launchCoroutineUI {
                    val offers = viewModel.offersChannel.consume { receive() }
                    val interests = viewModel.interestsChannel.consume { receive() }
                    open(when (profileModel.accountType) {
                        AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profileModel, offers, interests)
                        AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profileModel, offers, interests)
                    })
                }
            }
        } else {
            view.ivProfileMenu.visibility = View.VISIBLE
            view.ivProfileMenu.setOnClickListener {
                onSettingsClick(profileModel, it)
            }
        }
    }

    private fun setTitle(profileModel: ProfileAccountModel, view: View) {
        if (profileModel.accountType == AccountType.PERSONAL) {
            view.profileName.text = profileModel.formattedName
            view.profileSubName.text = profileModel.formattedPosition
        } else {
            view.profileName.text = profileModel.organizationInfo?.organizationName
            view.profileSubName.text = profileModel.organizationType
        }
    }

    companion object {
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val OTHER = "other"

        fun newInstance(account: ShortAccountModel): ProfileController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, account)
            params.putSerializable(EXTRA_ACCOUNT_ID, account.id)
            return ProfileController(params)
        }

        fun newInstance(accountId: String): ProfileController {
            val params = Bundle()
            params.putString(EXTRA_ACCOUNT_ID, accountId)
            return ProfileController(params)
        }
    }
}