package com.mnassa.screen.profile

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.View
import android.widget.Toast
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedPosition
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.profile.create.RecommendUserController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_profile.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController(data: Bundle) : MnassaControllerImpl<ProfileViewModel>(data), ComplaintOtherController.OnComplaintResult {

    override val layoutId: Int = R.layout.controller_profile
    override val viewModel: ProfileViewModel by instance()
    private val accountModel: ShortAccountModel? by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel? }
    private val accountId: String by lazy { args.getString(EXTRA_ACCOUNT_ID) }
    private var adapter = ProfileAdapter()
    private val dialog: DialogHelper by instance()
    private lateinit var profileId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)
        profileId = accountModel?.let {
            view.ivCropImage.avatarSquare(it.avatar)
            it.id
        } ?: run { accountId }
        viewModel.getProfileWithAccountId(profileId)
        adapter.onConnectionStatusClickListener = {
            when (it) {
                ConnectionStatus.CONNECTED -> dialog.yesNoDialog(view.context, fromDictionary(R.string.user_profile_you_want_to_disconnect)) {
                    viewModel.sendConnectionStatus(it, profileId)
                }
                ConnectionStatus.SENT, ConnectionStatus.RECOMMENDED, ConnectionStatus.REQUESTED ->
                    viewModel.sendConnectionStatus(it, profileId)
            }
        }
        adapter.onWalletClickListener = { open(WalletController.newInstance()) }
        adapter.onConnectionsClickListener = { open(AllConnectionsController.newInstance()) }
        view.ivProfileBack.setOnClickListener { close() }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = { open(CreateNeedController.newInstance()) }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        launchCoroutineUI {
            viewModel.profileChannel.consumeEach { profileModel ->
                adapter.profileModel = profileModel
                view.rvProfile.layoutManager = LinearLayoutManager(view.context)
                view.rvProfile.adapter = adapter
                profileModel.profile.avatar?.let { avatar ->
                    view.ivCropImage.avatarSquare(profileModel.profile.avatar)
                    view.ivCropImage.setOnClickListener {
                        PhotoPagerActivity.start(view.context, listOf(avatar), 0)
                    }
                }
                handleCollapsingToolbar(view, profileModel)
                setTitle(profileModel, view)
                onEditProfile(profileModel, view)
                if (!profileModel.isMyProfile) {
                    handleFab(profileModel.connectionStatus, view.fabProfile)
                }
            }
        }
        viewModel.getPostsById(profileId)

        adapter.isLoadingEnabled = savedInstanceState == null
        launchCoroutineUI {
            viewModel.postChannel.openSubscription().bufferize(this@ProfileController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        if (it.item.isNotEmpty()) {
                            adapter.isLoadingEnabled = false
                            adapter.dataStorage.addAll(it.item)
                        }
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> adapter.dataStorage.clear()
                }

            }
        }
        launchCoroutineUI {
            viewModel.statusesConnectionsChannel.consumeEach { connectionStatus ->
                adapter.profileModel?.let {
                    if (!it.isMyProfile) {
                        handleFab(connectionStatus, view.fabProfile)
                    }
                    it.connectionStatus = connectionStatus
                    adapter.notifyItemChanged(0)
                }
            }
        }
    }

    private fun handleCollapsingToolbar(view: View, profileModel: ProfileModel) {

        view.appBarLayout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            val shouldShowFab = (profileModel.connectionStatus == ConnectionStatus.CONNECTED ||
                    profileModel.connectionStatus == ConnectionStatus.RECOMMENDED ||
                    profileModel.connectionStatus == ConnectionStatus.SENT)
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                view.tvTitleCollapsed.visibility = View.VISIBLE
                if (!profileModel.isMyProfile && shouldShowFab) {
                    view.fabProfile.hide()
                }
            } else {
                view.tvTitleCollapsed.visibility = View.GONE
                if (!profileModel.isMyProfile && shouldShowFab) {
                    view.fabProfile.show()
                }
            }
        })
    }

    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(profileId, OTHER, value)
        }

    override fun onDestroyView(view: View) {
        view.rvProfile.adapter = null
        super.onDestroyView(view)
    }

    private fun handleFab(connectionStatus: ConnectionStatus, fab: FloatingActionButton) {
        when (connectionStatus) {
            ConnectionStatus.CONNECTED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener { open(ChatMessageController.newInstance(requireNotNull(adapter.profileModel).profile)) }
                fab.setImageResource(R.drawable.ic_chat)
            }
            ConnectionStatus.RECOMMENDED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener {
                    viewModel.sendConnectionStatus(connectionStatus, profileId)
                }
                fab.setImageResource(R.drawable.ic_new_requests)
            }
            ConnectionStatus.SENT -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener {
                    viewModel.sendConnectionStatus(connectionStatus, profileId)
                }
                fab.setImageResource(R.drawable.ic_pending)
            }
            else -> {
                fab.visibility = View.GONE
                fab.setOnClickListener(null)
            }
        }
    }

    private fun onSettingsClick(profileModel: ProfileModel, view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.user_profile_item, popup.menu)
        popup.menu.findItem(R.id.action_share_profile).title = "Share Profile" //todo set from dict
        popup.menu.findItem(R.id.action_complain_about_profile).title = "Complain about Profile" //todo set from dict
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share_profile -> open(RecommendUserController.newInstance(profileModel))
                R.id.action_complain_about_profile -> complainAboutProfile(profileModel, view)
            }
            true
        }
        popup.show()
    }

    private fun complainAboutProfile(profileModel: ProfileModel, view: View) {
        launchCoroutineUI {
            val reportsList = viewModel.retrieveComplaints()
            dialog.showComplaintDialog(view.context, reportsList) {
                if (it.id == OTHER) {
                    val controller = ComplaintOtherController.newInstance()
                    controller.targetController = this@ProfileController
                    open(controller)
                } else {
                    viewModel.sendComplaint(profileModel.profile.id, it.id, null)
                }
            }
        }
    }

    private fun onEditProfile(profileModel: ProfileModel, view: View) {
        if (profileModel.isMyProfile) {
            view.ivProfileEdit.visibility = View.VISIBLE
            view.ivProfileEdit.setOnClickListener {
                open(when (profileModel.profile.accountType) {
                    AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profileModel)
                    AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profileModel)
                })
            }
        } else {
            view.ivProfileMenu.visibility = View.VISIBLE
            view.ivProfileMenu.setOnClickListener {
                onSettingsClick(profileModel, it)
            }
        }
    }

    private fun setTitle(profileModel: ProfileModel, view: View) {
        if (profileModel.profile.accountType == AccountType.PERSONAL) {
            view.profileName.text = profileModel.formattedName
            view.profileSubName.text = profileModel.formattedPosition
            view.tvTitleCollapsed.text = profileModel.formattedName
        } else {
            view.profileName.text = profileModel.profile.organizationInfo?.organizationName
            view.profileSubName.text = profileModel.profile.organizationType
            view.tvTitleCollapsed.text = profileModel.profile.organizationInfo?.organizationName
        }
    }

    companion object {
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val OTHER = "other"

        fun newInstance(account: ShortAccountModel): ProfileController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, account)
            return ProfileController(params)
        }

        fun newInstance(accountId: String): ProfileController {
            val params = Bundle()
            params.putString(EXTRA_ACCOUNT_ID, accountId)
            return ProfileController(params)
        }
    }
}