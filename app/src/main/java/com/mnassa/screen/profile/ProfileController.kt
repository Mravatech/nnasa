package com.mnassa.screen.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.mnassa.screen.photopager.PhotoPagerController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.attachPanel
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.profile.create.RecommendUserController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
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
    private var adapter = ProfilePostsRVAdapter(this@ProfileController, profile)

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
        adapter.onPostedByClickListener = { if (it.id != profile.id) open(ProfileController.newInstance(it)) }
        adapter.onRepostedByClickListener = { if (it.id != profile.id) open(ProfileController.newInstance(it)) }
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onConnectionsCountClick = { if (profile.isMyProfile) open(AllConnectionsController.newInstance()) }
        adapter.onPointsCountClick = { if (profile.isMyProfile) open(WalletController.newInstance()) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onConnectionStatusClick = {
            view?.context?.let { context ->
                when (it) {
                    ConnectionStatus.CONNECTED -> dialog.yesNoDialog(context, fromDictionary(R.string.user_profile_you_want_to_disconnect)) {
                        viewModel.sendConnectionStatus(it, accountId)
                    }
                    ConnectionStatus.SENT, ConnectionStatus.RECOMMENDED, ConnectionStatus.REQUESTED ->
                        viewModel.sendConnectionStatus(it, accountId)
                }
            }
        }

        launchCoroutineUI {
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

            val titleColor = ContextCompat.getColor(context, R.color.white)
            collapsingToolbarLayout.setCollapsedTitleTextColor(titleColor)
            collapsingToolbarLayout.setExpandedTitleColor(titleColor)

            fabProfile.isGone = profile.isMyProfile
            fabProfile.setOnClickListener {
                launchCoroutineUI {
                    viewModel.profileChannel.consume { receiveOrNull() }?.apply { open(ChatMessageController.newInstance(this)) }
                }
            }

            rvProfile.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    viewModel.onScroll(visibleItemCount, totalItemCount, firstVisibleItemPosition)
                }
            })

            toolbar.apply {
                setNavigationOnClickListener { close() }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit_profile -> openEditProfile()
                        R.id.action_share_profile -> openShareProfile()
                        R.id.action_complain_about_profile -> openComplainProfile()
                        R.id.action_invite_to_group_profile -> openInviteToGroup()
                    }
                    true
                }
            }
        }

        launchCoroutineUI {
            bindProfile(getViewSuspend(), profile)
            viewModel.profileChannel.consumeEach { bindProfile(getViewSuspend(), it) }
        }

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }
        launchCoroutineUI { viewModel.offersChannel.consumeEach { bindOffers(getViewSuspend(), it) } }
        launchCoroutineUI { viewModel.interestsChannel.consumeEach { bindInterests(getViewSuspend(), it) } }
        launchCoroutineUI { viewModel.statusesConnectionsChannel.consumeEach { bindConnectionStatus(getViewSuspend(), it) } }
    }

    private suspend fun bindProfileToToolbar(toolbar: Toolbar, profile: ShortAccountModel) {
        toolbar.apply {
            // Clear previous menu and setup it
            // again.
            menu?.clear()
            inflateMenu(if (profile.isMyProfile) {
                R.menu.user_profile_my
            } else {
                R.menu.user_profile_other
            })
            menu.apply {
                findItem(R.id.action_edit_profile)?.title = fromDictionary(R.string.profile_edit)
                findItem(R.id.action_share_profile)?.title = fromDictionary(R.string.profile_share)
                findItem(R.id.action_complain_about_profile)?.title = fromDictionary(R.string.profile_report)
                findItem(R.id.action_invite_to_group_profile)?.title = fromDictionary(R.string.group_invite_profile_menu)

                if (!profile.canRecommend()) {
                    removeItem(R.id.action_share_profile)
                    removeItem(R.id.action_complain_about_profile)
                }
            }
        }
    }

    private suspend fun bindProfile(view: View, profile: ShortAccountModel) {
        this.profile = profile
        adapter.profile = profile
        with(view) {
            ivAvatarSquare.avatarSquare(profile.avatar)
            ivAvatarSquare.setOnClickListener {
                profile.avatar?.let { avatar ->
                    open(PhotoPagerController.newInstance(listOf(avatar)))
                }
            }
            collapsingToolbarLayout.title = profile.formattedName
            if (profile.accountType == AccountType.PERSONAL) {
                tvPosition.text = profile.formattedPosition
            } else {
                tvPosition.text = (profile as? ProfileAccountModel)?.organizationType
            }

            bindProfileToToolbar(toolbar, profile)
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

    override fun onDestroyView(view: View) {
        view.rvProfile.adapter = null
        super.onDestroyView(view)
    }

    private fun handleConnectionStatus(connectionStatus: ConnectionStatus, view: View) {
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

    // ---- Edit profile ----

    private fun openEditProfile() {
        launchCoroutineUI {
            val profile = getProfile() as? ProfileAccountModel ?: return@launchCoroutineUI
            val offers = viewModel.offersChannel.consume { receive() }
            val interests = viewModel.interestsChannel.consume { receive() }

            openEditProfile(profile, offers, interests)
        }
    }

    private fun openEditProfile(profile: ProfileAccountModel, offers: List<TagModel>, interests: List<TagModel>) =
        when (profile.accountType) {
            AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profile, offers, interests)
            AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profile, offers, interests)
        }.let(::open)

    // ---- Share profile ----

    private fun openShareProfile() {
        launchCoroutineUI {
            val profile = getProfile()
            RecommendUserController.newInstance(profile).let(::open)
        }
    }

    // ---- Complain profile ----

    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(accountId, OTHER, value)
        }

    private fun openComplainProfile() {
        launchCoroutineUI {
            val profile = getProfile()
            val complains = viewModel.retrieveComplaints()
            dialog.showComplaintDialog(getViewSuspend().context, complains) {
                if (it.id == OTHER) {
                    // Open other screen
                    ComplaintOtherController.newInstance().apply {
                        targetController = this@ProfileController
                    }.let(::open)
                } else {
                    viewModel.sendComplaint(profile.id, it.id, null)
                }
            }
        }
    }

    // ---- Invite to group ----

    private fun openInviteToGroup() {
        open(SelectGroupController.newInstance(this, onlyAdmin = true))
    }

    override fun onGroupSelected(group: GroupModel) {
        viewModel.inviteToGroup(group)
    }

    private suspend fun getProfile() = viewModel.profileChannel.consume { receiveOrNull() } ?: profile

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