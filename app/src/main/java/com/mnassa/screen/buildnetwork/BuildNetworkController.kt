package com.mnassa.screen.buildnetwork

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.main.MainController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_build_network.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/5/2018.
 */
class BuildNetworkController(args: Bundle) : MnassaControllerImpl<BuildNetworkViewModel>(args) {
    override val layoutId: Int = R.layout.controller_build_network
    override val viewModel: BuildNetworkViewModel by instance()
    private val dialogHelper: DialogHelper by instance()
    private val adapter = BuildNetworkAdapter()


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

            adapter.onSelectedAccountsChangedListener = { selectedAccounts ->
                tvInviteUsersToBuildNetwork.text = formatSubTitle()
                btnScreenHeaderAction.isEnabled = selectedAccounts.isNotEmpty()
            }

            btnSkipStep.text = fromDictionary(R.string.invite_skip_step)
            btnSkipStep.setOnClickListener {
                dialogHelper.showWelcomeDialog(it.context) {
                    open(MainController.newInstance())
                }
            }

            btnScreenHeaderAction.setOnClickListener {
                viewModel.inviteUsers(adapter.selectedAccounts.toList())
            }
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.usersToInviteChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
            }
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    is BuildNetworkViewModel.OpenScreenCommand.MainScreen -> {
                        view.btnSkipStep.performClick()
                    }
                }
            }
        }

        checkPermissions()
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    private var checkPermissionsJob: Job? = null
    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        checkPermissionsJob?.cancel()

        checkPermissionsJob = launchCoroutineUI {
            val permissionsResult = permissions.requestPermissions(Manifest.permission.READ_CONTACTS)
            if (permissionsResult.isAllGranted) {
                viewModel.onContactPermissionsGranted()
            } else {
                val view = view ?: return@launchCoroutineUI
                Snackbar.make(view, fromDictionary(R.string.tab_connections_contact_permissions_description), Snackbar.LENGTH_INDEFINITE)
                        .setAction(fromDictionary(R.string.tab_connections_contact_permissions_button)) {
                            if (permissionsResult.isShouldShowRequestPermissionRationale) {
                                checkPermissions()
                            } else {
                                view.context.openApplicationSettings()
                            }
                        }.show()
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

        fun newInstance(): BuildNetworkController {
            val args = Bundle()
            return BuildNetworkController(args)
        }
    }
}