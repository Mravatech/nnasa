package com.mnassa.screen.main

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.connections.ConnectionsController
import com.mnassa.screen.home.HomeController
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.notifications.NotificationsController
import kotlinx.android.synthetic.main.controller_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>() {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    PAGE_HOME -> HomeController.newInstance()
                    PAGE_CONNECTIONS -> ConnectionsController.newInstance()
                    PAGE_NOTIFICATIONS -> NotificationsController.newInstance()
                    PAGE_CHAT -> ChatListController.newInstance()
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = PAGES_COUNT
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.vpMain.adapter = adapter

        view.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    MainViewModel.ScreenType.LOGIN -> EnterPhoneController.newInstance()
                }

                router.replaceTopController(RouterTransaction.with(controller))
            }
        }
    }

    override fun onViewDestroyed(view: View) {
        if (!requireNotNull(activity).isChangingConfigurations) {
            view.vpMain.adapter = null
        }
        super.onViewDestroyed(view)
    }


    companion object {
        private const val PAGES_COUNT = 4
        private const val PAGE_HOME = 0
        private const val PAGE_CONNECTIONS = 1
        private const val PAGE_NOTIFICATIONS = 2
        private const val PAGE_CHAT = 3

        fun newInstance() = MainController()
    }
}