package com.mnassa.screen.connections.recommended

import android.Manifest
import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_recommended.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 09.03.2018.
 */
class RecommendedConnectionsController : MnassaControllerImpl<RecommendedConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_recommended
    override val viewModel: RecommendedConnectionsViewModel by instance()
    private val adapter = GroupedRecommendedConnectionsRVAdapter()

    private var permissionsSnackbar: Snackbar? = null
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onConnectClickListener = { viewModel.connect(it) }
        adapter.onItemClickListener = { open(ProfileController.newInstance(it)) }

        with(view) {
            toolbar.title = fromDictionary(R.string.recommended_connections_title)

            rvRecommendedConnections.layoutManager = LinearLayoutManager(context)
            rvRecommendedConnections.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
                view.rlEmptyView.visibility = if (it.isEmpty) View.VISIBLE else View.INVISIBLE
            }
        }
        sendContacts()
    }


    private var sendContactsJob: Job? = null
    @SuppressLint("MissingPermission")
    private fun sendContacts() {
        sendContactsJob?.cancel()
        sendContactsJob = launchCoroutineUI {
            val permissionsResult = permissions.requestPermissions(Manifest.permission.READ_CONTACTS)

            if (permissionsResult.isAllGranted) {
                permissionsSnackbar?.dismiss()
                viewModel.onContactPermissionsGranted()
            } else {
                val view = view?.clSnackbarParent ?: return@launchCoroutineUI
                if (permissionsSnackbar?.isShown != true) {
                    permissionsSnackbar = Snackbar.make(view, fromDictionary(R.string.tab_connections_contact_permissions_description), Snackbar.LENGTH_INDEFINITE)
                            .setAction(fromDictionary(R.string.tab_connections_contact_permissions_button)) {
                                if (permissionsResult.isShouldShowRequestPermissionRationale) {
                                    sendContacts()
                                } else {
                                    view.context.openApplicationSettings()
                                }
                            }
                    permissionsSnackbar?.show()
                }
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvRecommendedConnections.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = RecommendedConnectionsController()
    }
}