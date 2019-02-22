package com.mnassa.screen.connections

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.isGone
import com.mnassa.extensions.openApplicationSettings
import com.mnassa.extensions.setHeaderWithCounter
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.NewConnectionRequestsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.RecommendedConnectionsRecyclerViewAdapter
import com.mnassa.screen.connections.archived.ArchivedConnectionController
import com.mnassa.screen.connections.newrequests.NewRequestsController
import com.mnassa.screen.connections.recommended.RecommendedConnectionsController
import com.mnassa.screen.connections.sent.SentConnectionsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections.view.*
import kotlinx.android.synthetic.main.controller_connections_header.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsController : MnassaControllerImpl<ConnectionsViewModel>(), OnPageSelected {
    override val layoutId: Int = R.layout.controller_connections
    override val viewModel: ConnectionsViewModel by instance()
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter(true)
    private val recommendedConnectionsAdapter = RecommendedConnectionsRecyclerViewAdapter()
    private val newConnectionRequestsAdapter = NewConnectionRequestsRecyclerViewAdapter()
    private var permissionsSnackbar: Snackbar? = null
    private val dialog: DialogHelper by instance()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            allConnectionsAdapter.restoreState(this)
            recommendedConnectionsAdapter.restoreState(this)
            newConnectionRequestsAdapter.restoreState(this)
        }

        recommendedConnectionsAdapter.onShowAllClickListener = { openRecommendedConnectionsScreen() }
        recommendedConnectionsAdapter.onConnectClickListener = { viewModel.connect(it) }
        recommendedConnectionsAdapter.onItemClickListener = { open(ProfileController.newInstance(it)) }

        newConnectionRequestsAdapter.onAcceptClickListener = { viewModel.accept(it) }
        newConnectionRequestsAdapter.onDeclineClickListener = { account ->
            view?.let { view ->
                launchCoroutineUI {
                    val disconnectDays = viewModel.getDisconnectTimeoutDays()
                    dialog.showDeclineConnectionDialog(view.context, disconnectDays) {
                        viewModel.decline(account)
                    }
                }
            }
        }
        newConnectionRequestsAdapter.onItemClickListener = { open(ProfileController.newInstance(it)) }
        newConnectionRequestsAdapter.onShowAllClickListener = { openNewRequestsScreen() }

        allConnectionsAdapter.isLoadingEnabled = savedInstanceState == null
        allConnectionsAdapter.onBindHeader = { bindHeader(it) }
        allConnectionsAdapter.onItemOptionsClickListener = { item, view -> onMoreConnectedAccountFunctions(item, view) }
        allConnectionsAdapter.onItemClickListener = { open(ProfileController.newInstance(it)) }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.backButtonEnabled = false
            toolbar.title = fromDictionary(R.string.tab_connections_title)

            rvAllConnections.adapter = allConnectionsAdapter

            toolbar.onMoreClickListener = {
                popupMenuHelper.showConnectionsTabMenu(
                        view = it,
                        openRecommendedConnectionsScreen = { openRecommendedConnectionsScreen() },
                        openSentRequestsScreen = { openSentRequestsScreen() },
                        openArchivedConnectionsScreen = { openArchivedConnectionsScreen() }
                )
            }

            searchView.etSearch.addTextChangedListener(SimpleTextWatcher {
                allConnectionsAdapter.searchByName(it)
                recommendedConnectionsAdapter.searchByName(it)
                newConnectionRequestsAdapter.searchByName(it)
            })
        }
    }

    override fun onDestroyView(view: View) {
        permissionsSnackbar = null
        view.rvAllConnections.adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        allConnectionsAdapter.saveState(outState)
        recommendedConnectionsAdapter.saveState(outState)
        newConnectionRequestsAdapter.saveState(outState)
    }

    override fun onPageSelected() {
        askPermissions()
        view?.rvAllConnections?.scrollToPosition(0)
    }

    private var askPermissionsJob: Job? = null
    @SuppressLint("MissingPermission")
    private fun askPermissions() {
        askPermissionsJob?.cancel()
        askPermissionsJob = launchCoroutineUI {
            val permissionsResult = permissions.requestPermissions(Manifest.permission.READ_CONTACTS)

            if (permissionsResult.isAllGranted) {
                permissionsSnackbar?.dismiss()
                viewModel.onContactPermissionsGranted()
            } else {
                val view = getViewSuspend().clSnackbarParent ?: return@launchCoroutineUI
                if (permissionsSnackbar?.isShown != true) {
                    permissionsSnackbar = Snackbar.make(view, fromDictionary(R.string.tab_connections_contact_permissions_description), Snackbar.LENGTH_INDEFINITE)
                            .setAction(fromDictionary(R.string.tab_connections_contact_permissions_button)) {
                                if (permissionsResult.isShouldShowRequestPermissionRationale) {
                                    askPermissions()
                                } else {
                                    view.context.openApplicationSettings()
                                }
                            }
                    permissionsSnackbar?.show()
                }
            }
        }
    }

    private var loadAllConnectionsJob: Job? = null
    private var loadRecommendedConnectionsJob: Job? = null
    private var loadNewConnectionsJob: Job? = null

    private fun bindHeader(header: View) {
        with(header) {
            tvNewConnectionRequests.text = fromDictionary(R.string.tab_connections_new_requests)
            tvRecommendedConnections.text = fromDictionary(R.string.tab_connections_recommended)
            tvAllConnections.text = fromDictionary(R.string.tab_connections_all)

            rvNewConnectionRequests.itemAnimator = null
            rvNewConnectionRequests.isNestedScrollingEnabled = false

            rvRecommendedConnections.adapter = recommendedConnectionsAdapter
            rvNewConnectionRequests.adapter = newConnectionRequestsAdapter

            rvRecommendedConnections.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvNewConnectionRequests.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)

            //
            allConnectionsAdapter.onAfterSearchListener = {
                val allSize = allConnectionsAdapter.dataStorage.size
                tvAllConnections.isGone = allSize == 0
                tvAllConnections.setHeaderWithCounter(R.string.tab_connections_all, allSize)
            }
            recommendedConnectionsAdapter.onAfterSearchListener = {
                val recommendSize = if (it == 0) recommendedConnectionsAdapter.dataStorage.size else it + recommendedConnectionsAdapter.dataStorage.size
                rvRecommendedConnections.isGone = recommendSize == 0
                tvRecommendedConnections.isGone = recommendSize == 0
                tvRecommendedConnections.setHeaderWithCounter(R.string.tab_connections_recommended, recommendSize)
            }
            newConnectionRequestsAdapter.onAfterSearchListener = {
                val newSize = if (it == 0) newConnectionRequestsAdapter.dataStorage.size else it + newConnectionRequestsAdapter.dataStorage.size
                rvNewConnectionRequests.isGone = newSize == 0
                tvNewConnectionRequests.isGone = newSize == 0
                tvNewConnectionRequests.setHeaderWithCounter(R.string.tab_connections_new_requests, newSize)
            }
        }

        val headerRef = header.asReference()

        loadAllConnectionsJob?.cancel()
        loadAllConnectionsJob = launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                allConnectionsAdapter.isLoadingEnabled = false
                allConnectionsAdapter.set(it)

                with(headerRef()) {
                    tvAllConnections.setHeaderWithCounter(R.string.tab_connections_all, it.size)
                    tvAllConnections.isGone = it.isEmpty()
                    vAllConnections.isGone = it.isEmpty()
                }
            }
        }

        loadRecommendedConnectionsJob?.cancel()
        loadRecommendedConnectionsJob = launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                recommendedConnectionsAdapter.setWithMaxRange(it)

                with(headerRef()) {
                    tvRecommendedConnections.setHeaderWithCounter(R.string.tab_connections_recommended, it.size)
                    tvRecommendedConnections.isGone = it.isEmpty()
                    rvRecommendedConnections.isGone = it.isEmpty()
                }
            }
        }

        loadNewConnectionsJob?.cancel()
        loadNewConnectionsJob = launchCoroutineUI {
            viewModel.newConnectionRequestsChannel.consumeEach {
                newConnectionRequestsAdapter.setWithMaxRange(it)

                with(headerRef()) {
                    tvNewConnectionRequests.isGone = it.isEmpty()
                    rvNewConnectionRequests.isGone = it.isEmpty()
                    vNewConnectionRequests.isGone = it.isEmpty()

                    tvNewConnectionRequests.setHeaderWithCounter(R.string.tab_connections_new_requests, it.size)
                }
            }
        }
    }

    ///////////////////////////////////////// CONNECTION TYPE SCREENS ///////////////////////////////

    private fun openRecommendedConnectionsScreen() = open(RecommendedConnectionsController.newInstance())
    private fun openSentRequestsScreen() = open(SentConnectionsController.newInstance())
    private fun openArchivedConnectionsScreen() = open(ArchivedConnectionController.newInstance())
    private fun openNewRequestsScreen() = open(NewRequestsController.newInstance())
    private fun openChat(accountModel: ShortAccountModel) = open(ChatMessageController.newInstance(accountModel))
    private fun openProfile(accountModel: ShortAccountModel) = open(ProfileController.newInstance(accountModel))

    private fun onMoreConnectedAccountFunctions(accountModel: ShortAccountModel, sender: View) {
        launchCoroutineUI {
            popupMenuHelper.showConnectedAccountMenu(
                    view = sender,
                    account = accountModel,
                    onChat = { openChat(accountModel) },
                    onProfile = { openProfile(accountModel) },
                    onDisconnect = { viewModel.disconnect(accountModel) })
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    class BlockedScrollingLayoutManager(
            context: Context,
            orientation: Int,
            reverseLayout: Boolean
    ) : LinearLayoutManager(context, orientation, reverseLayout) {
        override fun canScrollHorizontally(): Boolean = false
        override fun canScrollVertically(): Boolean = false
    }

    companion object {
        fun newInstance() = ConnectionsController()
    }
}