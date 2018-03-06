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
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = PAGES_COUNT
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpMain.adapter = adapter
            bnMain.addItems(
                    mutableListOf(
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent)
                    )
            )

            bnMain.isBehaviorTranslationEnabled = false
            bnMain.setNotification("3", 1)
            bnMain.setOnTabSelectedListener { position, _ ->
                vpMain.setCurrentItem(position, false)
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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
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
            R.id.nav_share -> {

            }
            R.id.nav_logout -> {
                viewModel.logout()
            }
        }

        requireNotNull(view).drawerLayout.closeDrawer(GravityCompat.START)
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