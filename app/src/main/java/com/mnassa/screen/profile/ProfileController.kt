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
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.PostDetailsController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.screen.profile.model.ProfileModel
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        adapter.viewModel = viewModel
        view.ivProfileBack.setOnClickListener { close() }
        view.appBarLayout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                view.tvTitleCollapsed.visibility = View.VISIBLE
            } else {
                view.tvTitleCollapsed.visibility = View.GONE
            }
        })
        val id = accountModel?.let {
            view.ivCropImage.avatarSquare(it.avatar)
            it.id
        } ?: run { accountId }
        viewModel.getProfileWithAccountId(id)
        adapter.onItemClickListener = { open(PostDetailsController.newInstance(it)) }
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
                setTitle(profileModel, view)
                onEditProfile(profileModel, view)
                handleFab(profileModel.connectionStatus, view.fabProfile)
            }
        }
        viewModel.getPostsById(id)
        launchCoroutineUI {
            viewModel.postChannel.consumeEach {
                //TODO: bufferization
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.add(it.item)
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.remove(it.item)
                    is ListItemEvent.Cleared -> adapter.dataStorage.clear()
                }

            }
        }
        launchCoroutineUI {
            viewModel.profileClickChannel.consumeEach {
                when (it) {
                    is ProfileViewModel.ProfileCommand.ProfileConnection -> open(AllConnectionsController.newInstance())
                    is ProfileViewModel.ProfileCommand.ProfileWallet -> Toast.makeText(view.context, "ProfileWallet", Toast.LENGTH_SHORT).show()
                }
            }
        }
        launchCoroutineUI {
            viewModel.statusesConnectionsChannel.consumeEach {
                when (it) {
                    ConnectionStatus.CONNECTED -> dialog.connectionsDialog(view.context) {
                        viewModel.sendConnectionStatus(it, id, true)
                    }
                    ConnectionStatus.SENT -> dialog.connectionsDialog(view.context) {
                        //todo set later on
                        viewModel.sendConnectionStatus(it, id, true)
                    }
                    ConnectionStatus.RECOMMENDED -> dialog.connectionsDialog(view.context) {
                        viewModel.sendConnectionStatus(it, id, true)
                    }
                    ConnectionStatus.REQUESTED -> dialog.connectionsDialog(view.context) {
                        viewModel.sendConnectionStatus(it, id, true)
                    }
                }
            }
        }
    }

    override var onComplaint: String = ""
        set(value) {
            val id = accountModel?.id ?: accountId
            viewModel.sendComplaint(id, value)
        }

    private fun handleFab(connectionStatus: ConnectionStatus, fab: FloatingActionButton) {
        Toast.makeText(activity, connectionStatus.name, Toast.LENGTH_LONG).show()
        //todo handle here
        when (connectionStatus) {
            ConnectionStatus.CONNECTED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener { open(ChatListController.newInstance()) }
                fab.setImageResource(R.drawable.ic_chat)
            }
            ConnectionStatus.RECOMMENDED -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener { }
                fab.setImageResource(R.drawable.ic_recommend)//todo icons
            }
            ConnectionStatus.SENT -> {
                fab.visibility = View.VISIBLE
                fab.setOnClickListener { }
                fab.setImageResource(R.drawable.ic_camera)//todo icons
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
                R.id.action_share_profile -> Toast.makeText(view.context, "Share Profile", Toast.LENGTH_SHORT).show()
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
                    viewModel.sendComplaint(profileModel.profile.id, it.toString())
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
            view.profileName.text = "${profileModel.profile.personalInfo?.firstName
                    ?: ""} ${profileModel.profile.personalInfo?.lastName ?: ""}"
            view.profileSubName.text = profileModel.profile.abilities.firstOrNull { it.isMain }?.place
            view.tvTitleCollapsed.text = "${profileModel.profile.personalInfo?.firstName
                    ?: ""} ${profileModel.profile.personalInfo?.lastName ?: ""}"
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