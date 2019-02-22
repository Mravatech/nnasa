package com.mnassa.screen.buildnetwork

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_build_network.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

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

            toolbar.withActionButton(fromDictionary(R.string.invite_title_action)) {
                viewModel.inviteUsers(adapter.selectedAccounts.toList())
            }
            toolbar.actionButtonClickable = adapter.selectedAccounts.isNotEmpty()

            tvInviteUsersToBuildNetwork.text = formatSubTitle()

            rvInvite.adapter = adapter

            adapter.onSelectedAccountsChangedListener = { selectedAccounts ->
                tvInviteUsersToBuildNetwork.text = formatSubTitle()
                toolbar.actionButtonClickable = selectedAccounts.isNotEmpty()
            }

            btnSkipStep.text = fromDictionary(R.string.invite_skip_step)
            btnSkipStep.setOnClickListener {
                dialogHelper.showWelcomeDialog(it.context) {
                    open(MainController.newInstance())
                }
            }
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.usersToInviteChannel.consumeEach {
                if (it.isNotEmpty()) {
                    adapter.isLoadingEnabled = false
                    adapter.set(it)
                }
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
        view.rvInvite.adapter = null
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