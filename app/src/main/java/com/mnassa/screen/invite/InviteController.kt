package com.mnassa.screen.invite

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.main.MainController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/5/2018.
 */
class InviteController(args: Bundle) : MnassaControllerImpl<InviteViewModel>(args) {
    override val layoutId: Int = R.layout.controller_invite
    override val viewModel: InviteViewModel by instance()
    private val adapter = InviteAdapter()

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.BUILD_NETWORK

            tvScreenHeader.text = fromDictionary(R.string.invite_title)
            btnScreenHeaderAction.text = fromDictionary(R.string.invite_title_action)
            btnScreenHeaderAction.visibility = View.VISIBLE
            btnScreenHeaderAction.isEnabled = adapter.selectedAccounts.isNotEmpty()

            tvInviteUsersToBuildNetwork.text = formatSubTitle()

            rvInvite.layoutManager = LinearLayoutManager(view.context)
            rvInvite.adapter = adapter
//            rvInvite.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))

            adapter.onSelectedAccountsChangedListener = { selectedAccounts ->
                tvInviteUsersToBuildNetwork.text = formatSubTitle()
                btnScreenHeaderAction.isEnabled = selectedAccounts.isNotEmpty()
            }

            btnSkipStep.text = fromDictionary(R.string.invite_skip_step)
            btnSkipStep.setOnClickListener {
                open(MainController.newInstance())
            }

            btnScreenHeaderAction.setOnClickListener {
                viewModel.inviteUsers(adapter.selectedAccounts.toList())
            }
        }

        launchCoroutineUI {
            if (permissions.requestPermissions(Manifest.permission.READ_CONTACTS).isAllGranted) {
                viewModel.onContactPermissionsGranted()
            }
        }

        launchCoroutineUI {
            viewModel.usersToInviteChannel.consumeEach {
                adapter.set(it)
            }
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    is InviteViewModel.OpenScreenCommand.MainScreen -> {
                        view.btnSkipStep.performClick()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(EXTRA_SELECTED_ACCOUNTS, ArrayList(adapter.selectedAccounts))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        adapter.selectedAccounts = savedInstanceState.getStringArrayList(EXTRA_SELECTED_ACCOUNTS).toSet()
    }

    private fun formatSubTitle(): String {
        return fromDictionary(R.string.invite_description) + " (${adapter.selectedAccounts.size})"
    }

    companion object {
        private const val EXTRA_SELECTED_ACCOUNTS = "EXTRA_SELECTED_ACCOUNTS"

        fun newInstance(): InviteController {
            val args = Bundle()
            return InviteController(args)
        }
    }
}