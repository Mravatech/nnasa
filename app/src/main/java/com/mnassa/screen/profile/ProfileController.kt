package com.mnassa.screen.profile

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.View
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formattedPosition
import com.mnassa.extensions.isMyProfile
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

class ProfileController(data: Bundle) : MnassaControllerImpl<ProfileViewModel>(data), ComplaintOtherController.OnComplaintResult {

    override val layoutId: Int = R.layout.controller_profile
    private val accountId: String by lazy { args.getString(EXTRA_ACCOUNT_ID) }
    override val viewModel: ProfileViewModel by instance(arg = accountId)

    private var adapter = ProfileAdapter()
    private val dialog: DialogHelper by instance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)

        view.rvProfile.adapter = adapter
        view.ivProfileBack.setOnClickListener { close() }

        adapter.onConnectionStatusClickListener = {
            when (it) {
                ConnectionStatus.CONNECTED -> dialog.yesNoDialog(view.context, fromDictionary(R.string.user_profile_you_want_to_disconnect)) {
                    viewModel.sendConnectionStatus(it, accountId)
                }
                ConnectionStatus.SENT, ConnectionStatus.RECOMMENDED, ConnectionStatus.REQUESTED ->
                    viewModel.sendConnectionStatus(it, accountId)
            }
        }
        adapter.onWalletClickListener = { open(WalletController.newInstance()) }
        adapter.onConnectionsClickListener = { open(AllConnectionsController.newInstance()) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = { open(CreateNeedController.newInstance()) }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }

        launchCoroutineUI {
            viewModel.profileChannel.consumeEach { profileModel ->
                adapter.profileModel = profileModel

                profileModel.avatar?.let { avatar ->
                    view.ivCropImage.avatarSquare(profileModel.avatar)
                    view.ivCropImage.setOnClickListener {
                        PhotoPagerActivity.start(view.context, listOf(avatar), 0)
                    }
                }

                val connectionStatus = viewModel.statusesConnectionsChannel.consume { receive() }
                handleCollapsingToolbar(view, connectionStatus, profileModel)
                setTitle(profileModel, view)
                onEditProfile(profileModel, view)
                if (!profileModel.isMyProfile) {
                    handleFab(connectionStatus, view.fabProfile)
                }
            }
        }

        launchCoroutineUI {
            viewModel.offersChannel.consumeEach { adapter.offers = it }
        }

        launchCoroutineUI {
            viewModel.interestsChannel.consumeEach { adapter.interests = it }
        }

        launchCoroutineUI {
            viewModel.statusesConnectionsChannel.consumeEach { connectionStatus ->
                val profile = viewModel.profileChannel.consume { receive() }
                if (!profile.isMyProfile) {
                    handleFab(connectionStatus, view.fabProfile)
                }
            }
        }

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
    }

    private fun handleCollapsingToolbar(view: View, connectionStatus: ConnectionStatus, profileModel: ProfileAccountModel) {

        view.appBarLayout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            val shouldShowFab = (connectionStatus == ConnectionStatus.CONNECTED ||
                    connectionStatus == ConnectionStatus.RECOMMENDED ||
                    connectionStatus == ConnectionStatus.SENT)
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
            viewModel.sendComplaint(accountId, OTHER, value)
        }

    override fun onDestroyView(view: View) {
        view.rvProfile.adapter = null
        super.onDestroyView(view)
    }

    private fun handleFab(connectionStatus: ConnectionStatus, fab: FloatingActionButton) {
        when (connectionStatus) {
            ConnectionStatus.CONNECTED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener {
                    adapter.profileModel?.let {
                        open(ChatMessageController.newInstance(it))
                    }
                }
                fab.setImageResource(R.drawable.ic_chat)
            }
            ConnectionStatus.RECOMMENDED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener {
                    viewModel.sendConnectionStatus(connectionStatus, accountId)
                }
                fab.setImageResource(R.drawable.ic_new_requests)
            }
            ConnectionStatus.SENT -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener {
                    viewModel.sendConnectionStatus(connectionStatus, accountId)
                }
                fab.setImageResource(R.drawable.ic_pending)
            }
            else -> {
                fab.visibility = View.GONE
                fab.setOnClickListener(null)
            }
        }
    }

    private fun onSettingsClick(profileModel: ProfileAccountModel, view: View) {
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
            view.tvTitleCollapsed.text = profileModel.formattedName
        } else {
            view.profileName.text = profileModel.organizationInfo?.organizationName
            view.profileSubName.text = profileModel.organizationType
            view.tvTitleCollapsed.text = profileModel.organizationInfo?.organizationName
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