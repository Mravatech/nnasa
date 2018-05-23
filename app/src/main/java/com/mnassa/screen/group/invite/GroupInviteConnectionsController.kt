package com.mnassa.screen.group.invite

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_group_invite_connections.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/23/2018.
 */
class GroupInviteConnectionsController(args: Bundle) : MnassaControllerImpl<GroupInviteConnectionsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_invite_connections
    private val groupId by lazy { args.getString(EXTRA_GROUP_ID) }
    override val viewModel: GroupInviteConnectionsViewModel by instance(arg = groupId)
    private val adapter = BuildNetworkAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.group_select_apply)) {
                viewModel.invite(adapter.selectedAccounts.toList())
            }
            rvGroupInvite.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.connectionsChannel.consumeEach {
                adapter.set(it)
                adapter.isLoadingEnabled = false
            }
        }

        adapter.onSelectedAccountsChangedListener = { onInputChanged() }
        onInputChanged()

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }
    }

    override fun onDestroyView(view: View) {
        view.rvGroupInvite.adapter = null
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    private fun onInputChanged() {
        view?.toolbar?.actionButtonClickable = adapter.selectedAccounts.isNotEmpty()
    }

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance(group: GroupModel): GroupInviteConnectionsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)
            return GroupInviteConnectionsController(args)
        }
    }
}