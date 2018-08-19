package com.mnassa.screen.profile

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.select.SelectGroupController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.attachPanel
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.profile.create.RecommendUserController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance


class ProfileController(data: Bundle) : MnassaControllerImpl<ProfileViewModel>(data),
        ComplaintOtherController.OnComplaintResult,
        SelectGroupController.OnGroupSelectedListener,
        View.OnClickListener {

    override val layoutId: Int = R.layout.controller_profile
    private val accountId: String by lazy { args.getString(EXTRA_ACCOUNT_ID) }
    override val viewModel: ProfileViewModel by instance(arg = accountId)

    private val dialog: DialogHelper by instance()
    private var lastViewedPostDate: Long = -1
    private var hasNewPosts: Boolean = false
        get() {
            return lastViewedPostDate < getFirstItem()?.createdAt?.time ?: -1
        }
    private var profile: ShortAccountModel = args[EXTRA_ACCOUNT] as ShortAccountModel
    private var adapter = ProfilePostsRVAdapter(profile)

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        adapter.isLoadingEnabled = savedInstanceState == null
        adapter.onDataChangedListener = { itemsCount ->
            view?.findViewById<View>(R.id.rlEmptyView)?.isGone = !adapter.dataStorage.isEmpty()
        }
        adapter.onAttachedToWindow = { post ->
            if (post.createdAt.time > lastViewedPostDate) {
                lastViewedPostDate = post.createdAt.time
            }
        }
        adapter.onCreateNeedClickListener = { open(CreateNeedController.newInstance()) }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.postChannel.subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().findViewById(R.id.rlEmptyView) },
                    onAdded = { triggerScrollPanel() },
                    onCleared = { lastViewedPostDate = -1 }
            )
        }
    }

    private fun getFirstItem(): PostModel? {
        if (adapter.dataStorage.isEmpty()) return null
        return adapter.dataStorage[0]
    }

    private fun triggerScrollPanel() {
        view?.rvProfile?.scrollBy(0, 0)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvProfile.adapter = adapter
            rvProfile.attachPanel { hasNewPosts }

            initToolbar(toolbar, profile)

            val titleColor = ContextCompat.getColor(context, R.color.white)
            collapsingToolbarLayout.setCollapsedTitleTextColor(titleColor)
            collapsingToolbarLayout.setExpandedTitleColor(titleColor)


            bindProfile(this, profile)
        }

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }
        launchCoroutineUI { viewModel.profileChannel.consumeEach { bindProfile(view, it) } }
        launchCoroutineUI { viewModel.offersChannel.consumeEach { bindOffers(view, it) } }
        launchCoroutineUI { viewModel.interestsChannel.consumeEach { bindInterests(view, it) } }
        launchCoroutineUI { viewModel.statusesConnectionsChannel.consumeEach { bindConnectionStatus(view, it) } }
    }

    private fun initToolbar(toolbar: Toolbar, profile: ShortAccountModel) {
        toolbar.setNavigationOnClickListener { close() }
        if (profile.isMyProfile) {
            toolbar.inflateMenu(R.menu.user_profile_my)
        } else {
            toolbar.inflateMenu(R.menu.user_profile_other)
        }
        toolbar.menu.findItem(R.id.action_edit_profile)?.title = "Edit Profile" //todo set from dict
        toolbar.menu.findItem(R.id.action_share_profile)?.title = "Share Profile" //todo set from dict
        toolbar.menu.findItem(R.id.action_complain_about_profile)?.title = "Complain about Profile" //todo set from dict
        toolbar.menu.findItem(R.id.action_invite_to_group_profile)?.title = fromDictionary(R.string.group_invite_profile_menu)


        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_edit_profile -> onEditProfileClick(profile)
                R.id.action_share_profile -> open(RecommendUserController.newInstance(profile))
                R.id.action_complain_about_profile -> complainAboutProfile(profile)
                R.id.action_invite_to_group_profile -> open(SelectGroupController.newInstance(this, onlyAdmin = true))
            }
            true
        }
    }

    private fun bindProfile(view: View, profile: ShortAccountModel) {
        this.profile = profile
        adapter.profile = profile
        with(view) {
            ivAvatar.avatarSquare(profile.avatar)
            collapsingToolbarLayout.title = profile.formattedName
            if (profile.accountType == AccountType.PERSONAL) {
                tvPosition.text = profile.formattedPosition
            } else {
                tvPosition.text = (profile as? ProfileAccountModel)?.organizationType
            }
        }
    }

    private fun bindConnectionStatus(view: View, status: ConnectionStatus) {
        adapter.connectionStatus = status
        if (!profile.isMyProfile) {
            handleConnectionStatus(status, view)
        }
    }

    private fun bindOffers(view: View, offers: List<TagModel>) {
        adapter.offers = offers
    }

    private fun bindInterests(view: View, interests: List<TagModel>) {
        adapter.interests = interests
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

    override fun onGroupSelected(group: GroupModel) {
        viewModel.inviteToGroup(group)
    }

    private fun complainAboutProfile(profileModel: ShortAccountModel) {
        launchCoroutineUI {
            val reportsList = viewModel.retrieveComplaints()
            dialog.showComplaintDialog(getViewSuspend().context, reportsList) {
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

    @Suppress("NAME_SHADOWING")
    private fun onEditProfileClick(profile: ShortAccountModel) {
        var profile = profile as? ProfileAccountModel
        launchCoroutineUI {
            if (profile == null) {
                profile = viewModel.profileChannel.consume { receiveOrNull() }
            }
            val profile = profile ?: return@launchCoroutineUI
            val offers = viewModel.offersChannel.consume { receive() }
            val interests = viewModel.interestsChannel.consume { receive() }

            open(when (profile.accountType) {
                AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profile, offers, interests)
                AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profile, offers, interests)
            })
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
    }
}