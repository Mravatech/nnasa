package com.mnassa.screen.main

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.accountinfo.personal.PersonalInfoController
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.connections.ConnectionsController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.home.HomeController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.notifications.NotificationsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.screen.registration.RegistrationController
import kotlinx.android.synthetic.main.controller_main.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach


/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>(), NavigationView.OnNavigationItemSelectedListener, MnassaRouter {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    Pages.HOME.ordinal -> HomeController.newInstance()
                    Pages.CONNECTIONS.ordinal -> ConnectionsController.newInstance()
                    Pages.NOTIFICATIONS.ordinal -> NotificationsController.newInstance()
                    Pages.CHAT.ordinal -> ChatListController.newInstance()
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page).tag(formatTabControllerTag(position)))
            }
        }

        override fun getCount(): Int = Pages.values().size
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpMain.adapter = adapter
            vpMain.offscreenPageLimit = adapter.count

            bnMain.addItems(
                    //TODO: design needed
                    mutableListOf(
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent),
                            AHBottomNavigationItem(R.string.app_name, R.drawable.ic_home_white_24dp, R.color.colorAccent)
                    )
            )

            bnMain.setOnTabSelectedListener { position, _ ->
                vpMain.setCurrentItem(position, false)
                val page = adapter.getRouter(position)?.getControllerWithTag(formatTabControllerTag(position))
                if (page is OnPageSelected) {
                    page.onPageSelected()
                }

                true
            }
            navigationView.setNavigationItemSelectedListener(this@MainController)
        }

        launchCoroutineUI {
            viewModel.unreadChatsCountChannel.consumeEach { setCounter(Pages.CHAT.ordinal, it) }
        }
        launchCoroutineUI {
            viewModel.unreadNotificationsCountChannel.consumeEach { setCounter(Pages.NOTIFICATIONS.ordinal, it) }
        }
        launchCoroutineUI {
            viewModel.unreadConnectionsCountChannel.consumeEach { setCounter(Pages.CONNECTIONS.ordinal, it) }
        }
        launchCoroutineUI {
            viewModel.unreadEventsAndNeedsCountChannel.consumeEach { setCounter(Pages.HOME.ordinal, it) }
        }
        launchCoroutineUI {
            viewModel.currentAccountChannel.consumeEach {
                //TODO: design for side menu
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

        //TODO: design for notification badges
        val notification = if (counterValue != 0) AHNotification.Builder()
                .setText(counterValue.toString())
//                .setBackgroundColor(ContextCompat.getColor(this@DemoActivity, R.color.color_notification_back))
//                .setTextColor(ContextCompat.getColor(this@DemoActivity, R.color.color_notification_text))
                .build() else null
        view.bnMain.setNotification(notification, pageIndex)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        requireNotNull(view).drawerLayout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.nav_all_connections -> open(AllConnectionsController.newInstance())
            R.id.nav_build_network -> open(BuildNetworkController.newInstance())
            R.id.nav_change_account -> open(SelectAccountController.newInstance())
            R.id.nav_create_account -> open(RegistrationController.newInstance())
            R.id.personal_info -> open(PersonalInfoController.newInstance())
            R.id.nav_profile -> open(ProfileController.newInstance())
            R.id.nav_logout -> viewModel.logout()
        }

        return true
    }

    override fun handleBack(): Boolean {
        val view = requireNotNull(view)
        return if (view.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            view.drawerLayout.closeDrawer(GravityCompat.START)
            true
        } else {
            view.bnMain?.visibility = View.VISIBLE
            super.handleBack()
        }
    }

    override fun onViewDestroyed(view: View) {
        if (!requireNotNull(activity).isChangingConfigurations) {
            view.vpMain.adapter = null
        }
        super.onViewDestroyed(view)
    }

    override fun open(self: Controller, controller: Controller) {
        view?.bnMain?.visibility = View.GONE
        mnassaRouter.open(self, controller)

    }

    override fun close(self: Controller) = mnassaRouter.close(self)

    private fun formatTabControllerTag(position: Int): String {
        return "tab_controller_$position"
    }

    private enum class Pages {
        HOME, CONNECTIONS, NOTIFICATIONS, CHAT
    }

    companion object {
        fun newInstance() = MainController()
    }
}