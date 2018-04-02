package com.mnassa.screen.connections

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.salomonbrys.kodein.instance
import com.mnassa.App.Companion.context
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.extensions.setHeaderWithCounter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.NewConnectionRequestsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.RecommendedConnectionsRecyclerViewAdapter
import com.mnassa.screen.connections.archived.ArchivedConnectionController
import com.mnassa.screen.connections.newrequests.NewRequestsController
import com.mnassa.screen.connections.recommended.RecommendedConnectionsController
import com.mnassa.screen.connections.sent.SentConnectionsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections.view.*
import kotlinx.android.synthetic.main.controller_connections_header.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach


/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsController : MnassaControllerImpl<ConnectionsViewModel>(), OnPageSelected {
    override val layoutId: Int = R.layout.controller_connections
    override val viewModel: ConnectionsViewModel by instance()
    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter(true)
    private val recommendedConnectionsAdapter = RecommendedConnectionsRecyclerViewAdapter()
    private val newConnectionRequestsAdapter = NewConnectionRequestsRecyclerViewAdapter()
    private var isHeaderBounded = false
    private var permissionsSnackbar: Snackbar? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        recommendedConnectionsAdapter.onShowAllClickListener = { openRecommendedConnectionsScreen() }
        recommendedConnectionsAdapter.onConnectClickListener = { viewModel.connect(it) }

        newConnectionRequestsAdapter.onAcceptClickListener = { viewModel.accept(it) }
        newConnectionRequestsAdapter.onDeclineClickListener = { viewModel.decline(it) }
        newConnectionRequestsAdapter.onShowAllClickListener = { openNewRequestsScreen() }

        allConnectionsAdapter.onBindHeader = { bindHeader(it) }
        allConnectionsAdapter.onItemClickListener = { item, view -> onMoreConnectedAccountFunctions(item, view) }




        with(view) {
            toolbar.backButtonEnabled = false
            toolbar.title = fromDictionary(R.string.tab_connections_title)

            rvAllConnections.layoutManager = LinearLayoutManager(context)
            rvAllConnections.adapter = allConnectionsAdapter

            toolbar.onMoreClickListener = {
                //Creating the instance of PopupMenu
                val popup = PopupMenu(it.context, it)
                //Inflating the Popup using xml file
                popup.menuInflater.inflate(R.menu.connections_main, popup.menu)
                popup.menu.findItem(R.id.action_recommended_connections).title = fromDictionary(R.string.tab_connections_recommended)
                popup.menu.findItem(R.id.action_sent_requests).title = fromDictionary(R.string.tab_connections_new_requests)
                popup.menu.findItem(R.id.action_archived).title = fromDictionary(R.string.tab_connections_rejected)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_recommended_connections -> openRecommendedConnectionsScreen()
                        R.id.action_sent_requests -> openSentRequestsScreen()
                        R.id.action_archived -> openArchivedConnectionsScreen()
                    }
                    true
                }

                popup.show()
            }
        }
    }

    override fun onDestroyView(view: View) {
        allConnectionsAdapter.destroyCallbacks()
        recommendedConnectionsAdapter.destoryCallbacks()
        newConnectionRequestsAdapter.destroyCallbacks()
        isHeaderBounded = false
        permissionsSnackbar = null
        super.onDestroyView(view)
    }

    private var onPageSelectedJob: Job? = null
    @SuppressLint("MissingPermission")
    override fun onPageSelected() {
        onPageSelectedJob?.cancel()
        onPageSelectedJob = launchCoroutineUI {
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
                                    onPageSelected()
                                } else {
                                    view.context.openApplicationSettings()
                                }
                            }
                    permissionsSnackbar?.show()
                }
            }
        }
    }

    private fun bindHeader(header: View) {
        if (isHeaderBounded) return
        isHeaderBounded = true

        with(header) {
            tvNewConnectionRequests.text = fromDictionary(R.string.tab_connections_new_requests)
            tvRecommendedConnections.text = fromDictionary(R.string.tab_connections_recommended)
            tvAllConnections.text = fromDictionary(R.string.tab_connections_all)

            rvRecommendedConnections.adapter = recommendedConnectionsAdapter
            rvNewConnectionRequests.adapter = newConnectionRequestsAdapter

            rvRecommendedConnections.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvNewConnectionRequests.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        allConnectionsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                allConnectionsAdapter.isLoadingEnabled = false
                allConnectionsAdapter.set(it)
                header.tvAllConnections.setHeaderWithCounter(R.string.tab_connections_all, it.size)

                header.tvAllConnections.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                header.vAllConnections.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                recommendedConnectionsAdapter.setWithMaxRange(it, MAX_RECOMMENDED_ITEMS_COUNT)
                header.tvRecommendedConnections.setHeaderWithCounter(R.string.tab_connections_recommended, it.size)

                header.tvRecommendedConnections.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                header.rvRecommendedConnections.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        launchCoroutineUI {
            viewModel.newConnectionRequestsChannel.consumeEach {
                newConnectionRequestsAdapter.setWithMaxRange(it, MAX_REQUESTED_ITEMS_COUNT)
                header.tvNewConnectionRequests.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                header.rvNewConnectionRequests.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                header.vNewConnectionRequests.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

                view?.toolbar?.counter = it.size
                header.tvNewConnectionRequests.setHeaderWithCounter(R.string.tab_connections_new_requests, it.size)
            }
        }
    }


    ///////////////////////////////////////// CONNECTION TYPE SCREENS ///////////////////////////////

    private fun openRecommendedConnectionsScreen() = open(RecommendedConnectionsController.newInstance())
    private fun openSentRequestsScreen() = open(SentConnectionsController.newInstance())
    private fun openArchivedConnectionsScreen() = open(ArchivedConnectionController.newInstance())
    private fun openNewRequestsScreen() = open(NewRequestsController.newInstance())

    private fun openChat(accountModel: ShortAccountModel) {
        Toast.makeText(context, "Opening chat with user ${accountModel.formattedName}", Toast.LENGTH_SHORT).show()
    }

    private fun openProfile(accountModel: ShortAccountModel) {
        Toast.makeText(context, "Opening profile of ${accountModel.formattedName}", Toast.LENGTH_SHORT).show()

    }

    private fun onMoreConnectedAccountFunctions(accountModel: ShortAccountModel, sender: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(sender.context, sender)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.connections_item, popup.menu)
        popup.menu.findItem(R.id.action_connections_send_message).title = fromDictionary(R.string.tab_connections_all_item_send_message)
        popup.menu.findItem(R.id.action_connections_view_profile).title = fromDictionary(R.string.tab_connections_all_item_view_profile)

        val disconnectSpan = SpannableString(fromDictionary(R.string.tab_connections_all_item_disconnect))
        val disconnectTextColor = ContextCompat.getColor(context, R.color.red)
        disconnectSpan.setSpan(ForegroundColorSpan(disconnectTextColor), 0, disconnectSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.findItem(R.id.action_connections_disconnect).title = disconnectSpan

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_connections_send_message -> openChat(accountModel)
                R.id.action_connections_view_profile -> openProfile(accountModel)
                R.id.action_connections_disconnect -> viewModel.disconnect(accountModel)
            }
            true
        }

        popup.show()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    class BlockedScrollingLayoutManager(context: Context, orientation: Int,
                                        reverseLayout: Boolean) : LinearLayoutManager(context, orientation, reverseLayout) {
        override fun canScrollHorizontally(): Boolean = false
        override fun canScrollVertically(): Boolean = false
    }

    companion object {
        private const val MAX_RECOMMENDED_ITEMS_COUNT = 10
        private const val MAX_REQUESTED_ITEMS_COUNT = 2

        fun newInstance() = ConnectionsController()
    }
}