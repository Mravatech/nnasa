package com.mnassa.screen.invite.history

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_invite_history.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance


class HistoryController : MnassaControllerImpl<HistoryViewModel>() {
    override val layoutId = R.layout.controller_invite_history
    override val viewModel: HistoryViewModel by instance()
    private var adapter: InviteHistoryAdapter = InviteHistoryAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        launchCoroutineUI {
            viewModel.phoneContactChannel.consumeEach {
                adapter.setData(it)
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            searchView.visibility = View.VISIBLE
            searchView.etSearch.addTextChangedListener(SimpleTextWatcher { adapter.search(it) })
            rvInviteHistory.adapter = adapter
        }
    }

    override fun onDestroyView(view: View) {
        view.rvInviteHistory.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = HistoryController()
    }
}