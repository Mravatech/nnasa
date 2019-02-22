package com.mnassa.screen.connections.select

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_all.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/2/2018.
 */
class SelectConnectionController(args: Bundle) : MnassaControllerImpl<SelectConnectionViewModel>(args) {
    override val layoutId: Int = R.layout.controller_connections_all
    override val viewModel: SelectConnectionViewModel by instance(arg = getAdditionalData(args))
    private val adapter = SelectConnectionRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is OnProfileSelectedListener) {
            "targetController $targetController must implement ${OnProfileSelectedListener::class.java.name}"
        }

        adapter.onItemClickListener = { account, _ ->
            (targetController as OnProfileSelectedListener).selectedAccount = account
            close()
        }

        with(view) {
            toolbar.title = fromDictionary(R.string.select_recipient_title)
            rvAllConnections.adapter = adapter
            searchView.etSearch.addTextChangedListener(SimpleTextWatcher {
                adapter.searchByName(it)
            })
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvAllConnections.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun <T> newInstance(listener: T): SelectConnectionController where T : OnProfileSelectedListener, T : Controller {
            val result = SelectConnectionController(Bundle())
            result.targetController = listener
            return result
        }

        fun <T> newInstanceWithCommunityMembers(listener: T, communityId: String): SelectConnectionController where T : OnProfileSelectedListener, T : Controller {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, communityId)

            val result = SelectConnectionController(args)
            result.targetController = listener
            return result
        }

        fun getAdditionalData(args: Bundle): SelectConnectionViewModel.AdditionalData {
            return if (args.containsKey(EXTRA_GROUP_ID)) {
                SelectConnectionViewModel.AdditionalData.IncludeGroupMembers(args.getString(EXTRA_GROUP_ID))
            } else SelectConnectionViewModel.AdditionalData.Empty()
        }
    }

    interface OnProfileSelectedListener {
        var selectedAccount: ShortAccountModel?
    }
}