package com.mnassa.screen.chats

import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListController : MnassaControllerImpl<ChatListViewModel>() {
    override val layoutId: Int = R.layout.controller_chat_list
    override val viewModel: ChatListViewModel by instance()

    companion object {
        fun newInstance() = ChatListController()
    }
}