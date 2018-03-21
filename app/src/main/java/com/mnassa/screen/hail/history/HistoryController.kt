package com.mnassa.screen.hail.history

import android.support.v7.widget.LinearLayoutManager
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tvToolbarScreenHeader.text = fromDictionary(R.string.invite_invite_header)
        view.ivInvitesHistory.visibility = View.GONE
        launchCoroutineUI {
            viewModel.phoneContactChannel.consumeEach {
                view.rvInviteHistory.layoutManager = LinearLayoutManager(view.context)
                view.rvInviteHistory.adapter = InviteHistoryAdapter(it)
            }
        }
        viewModel.retrievePhoneContacts()
    }

    companion object {
        fun newInstance() = HistoryController()
    }

}