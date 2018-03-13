package com.mnassa.screen.main

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
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
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.mainAbility
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.goneIfEmpty
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.nav_header.view.*


/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>(), NavigationView.OnNavigationItemSelectedListener {
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
                router.setRoot(RouterTransaction.with(page).tag("page_$position"))
            }
        }

        override fun getCount(): Int = PAGES_COUNT
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpMain.adapter = adapter
            vpMain.offscreenPageLimit = PAGES_COUNT

            bnMain.addItems(
                    //TODO: design needed
                    mutableListOf(
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent)
                    )
            )

            bnMain.isBehaviorTranslationEnabled = false
            bnMain.setOnTabSelectedListener { position, _ ->
                vpMain.setCurrentItem(position, false)
                val page = adapter.getRouter(position)?.getControllerWithTag("page_$position")
                if (page is OnPageSelected) {
                    page.onPageSelected()
                }

                true
            }

            navigationView.setNavigationItemSelectedListener(this@MainController)
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    MainViewModel.ScreenType.LOGIN -> EnterPhoneController.newInstance()
                }
                router.replaceTopController(RouterTransaction.with(controller))
            }
        }

        launchCoroutineUI {
            viewModel.unreadChatsCountChannel.consumeEach { setCounter(PAGE_CHAT, it)}
        }
        launchCoroutineUI {
            viewModel.unreadNotificationsCountChannel.consumeEach { setCounter(PAGE_NOTIFICATIONS, it) }
        }
        launchCoroutineUI {
            viewModel.unreadConnectionsCountChannel.consumeEach { setCounter(PAGE_CONNECTIONS, it) }
        }
        launchCoroutineUI {
            viewModel.unreadEventsAndNeedsCountChannel.consumeEach { setCounter(PAGE_HOME, it) }
        }
        launchCoroutineUI {
            viewModel.currentAccountChannel.consumeEach {
                view.ivUserAvatar?.avatarRound(it.avatar)
                view.tvUserName?.text = it.formattedName
                view.tvUserPosition?.text = it.id
//                    view.tvUserPosition.text = it.mainAbility(fromDictionary(R.string.invite_at_placeholder))
//                    view.tvUserPosition.goneIfEmpty()

            }
        }

    }

    private fun setCounter(pageIndex: Int, counterValue: Int) {
        val view = view ?: return
        val notification = if (counterValue != 0) AHNotification.Builder()
                .setText(counterValue.toString())
//                .setBackgroundColor(ContextCompat.getColor(this@DemoActivity, R.color.color_notification_back))
//                .setTextColor(ContextCompat.getColor(this@DemoActivity, R.color.color_notification_text))
                .build()  else null
        view.bnMain.setNotification(notification, pageIndex)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        requireNotNull(view).drawerLayout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_change_account -> {
                router.replaceTopController(RouterTransaction.with(SelectAccountController.newInstance()))
            }
            R.id.nav_create_account -> {
                router.pushController(RouterTransaction.with(RegistrationController.newInstance()))
            }
            R.id.nav_logout -> {
                viewModel.logout()
            }
        }


        return true
    }

    override fun handleBack(): Boolean {
        val view = requireNotNull(view)
        return if (view.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            view.drawerLayout.closeDrawer(GravityCompat.START)
            true
        } else {
            super.handleBack()
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