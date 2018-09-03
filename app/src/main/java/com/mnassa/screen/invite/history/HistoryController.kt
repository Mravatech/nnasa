package com.mnassa.screen.invite.history

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_history.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */

class HistoryController : MnassaControllerImpl<HistoryViewModel>() {
    override val layoutId = R.layout.controller_invite_history
    override val viewModel: HistoryViewModel by instance()
    private var adapter: InviteHistoryAdapter = InviteHistoryAdapter()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view){
            toolbar.title = fromDictionary(R.string.invite_invite_header)
            searchView.visibility = View.VISIBLE
            searchView.etSearch.addTextChangedListener(SimpleTextWatcher {
                adapter.search(it)
            })
        }
        launchCoroutineUI {
            viewModel.phoneContactChannel.consumeEach {
                view.rvInviteHistory.layoutManager = LinearLayoutManager(view.context)
                adapter.setData(it)
                view.rvInviteHistory.adapter = adapter
            }
        }
        viewModel.retrievePhoneContacts()
    }

    override fun onDestroyView(view: View) {
        view.rvInviteHistory.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = HistoryController()
    }
}