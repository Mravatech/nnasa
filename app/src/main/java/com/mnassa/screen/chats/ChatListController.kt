package com.mnassa.screen.chats

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import kotlinx.android.synthetic.main.controller_chat_list.view.*

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListController : MnassaControllerImpl<ChatListViewModel>() {
    override val layoutId: Int = R.layout.controller_chat_list
    override val viewModel: ChatListViewModel by instance()


    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.fabAddChat.setOnClickListener { open(AllConnectionsController.newInstance()) }

        launchCoroutineUI {

        }

//        llEmptyMessages
//        rvMessages
    }

    companion object {
        fun newInstance() = ChatListController()
    }
}