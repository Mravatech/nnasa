package com.mnassa.screen.invite.history

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_invite_history.view.*
import kotlinx.android.synthetic.main.toolbar_invite.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */

class HistoryController : MnassaControllerImpl<HistoryViewModel>() {
    override val layoutId = R.layout.controller_invite_history
    override val viewModel: HistoryViewModel by instance()
    private var adapter: InviteHistoryAdapter? = null
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tvToolbarScreenHeader.text = fromDictionary(R.string.invite_invite_header)
        view.ivInvitesHistory.visibility = View.GONE
        view.ivInvitesSearch.visibility = View.VISIBLE
        view.ivInvitesSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter?.search(newText)
                return false
            }
        })
        launchCoroutineUI {
            viewModel.phoneContactChannel.consumeEach {
                adapter = InviteHistoryAdapter(it.toMutableList())
                view.rvInviteHistory.layoutManager = LinearLayoutManager(view.context)
                view.rvInviteHistory.adapter = adapter
            }
        }
        viewModel.retrievePhoneContacts()
    }

    companion object {
        fun newInstance() = HistoryController()
    }

}