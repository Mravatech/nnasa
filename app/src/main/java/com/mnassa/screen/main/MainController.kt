package com.mnassa.screen.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.salomonbrys.kodein.instance
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedPosition
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.connections.ConnectionsController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.home.HomeController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.notifications.NotificationsController
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.screen.wallet.WalletController
import kotlinx.android.synthetic.main.controller_main.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>(), MnassaRouter {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()
    private var drawer: Drawer? = null
    private var accountHeader: AccountHeader? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)

        launchCoroutineUI {
            viewModel.availableAccountsChannel.consumeEach { accounts ->
                accountHeader?.apply {
                    clear()
                    accounts.forEach { profile ->
                        ProfileDrawerItem()
                                .withName(profile.formattedName)
                                .withEmail(profile.formattedPosition.toString())
                                .withIcon(profile.avatar)
                    }
                }
            }
        }

        with(view) {
            vpMain.adapter = adapter
            vpMain.offscreenPageLimit = adapter.count

            // Create a few sample profile
//            val profile = ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon(R.drawable.ic_archive)
//            val profile2 = ProfileDrawerItem().withName("Max Muster").withEmail("max.mustermann@gmail.com").withIcon(R.drawable.ic_archive)
//            val profile3 = ProfileDrawerItem().withName("Felix House").withEmail("felix.house@gmail.com").withIcon(R.drawable.ic_archive)
//            val profile4 = ProfileDrawerItem().withName("Mr. X").withEmail("mister.x.super@gmail.com").withIcon(R.drawable.ic_archive)
//            val profile5 = ProfileDrawerItem().withName("Batman").withEmail("batman@gmail.com").withIcon(R.drawable.ic_archive)

            accountHeader = AccountHeaderBuilder()
                    .withActivity(requireNotNull(activity))
                    .withTextColor(Color.BLACK)
                    .withSavedInstance(savedInstanceState)
                    .build()

            drawer = DrawerBuilder(requireNotNull(activity))
                    .withRootView(root)
                    .withAccountHeader(requireNotNull(accountHeader))
                    .addDrawerItems(
                            PrimaryDrawerItem().withName("Hi1").withIcon(R.drawable.ic_archive).withIdentifier(1)
                    )
                    .withSavedInstance(savedInstanceState)
                    .buildForFragment()

            bnMain.titleState = AHBottomNavigation.TitleState.ALWAYS_HIDE
            bnMain.accentColor = ContextCompat.getColor(view.context, R.color.accent)
            bnMain.addItems(
                    mutableListOf(
                            AHBottomNavigationItem(0, R.drawable.ic_tab_home, R.color.accent),
                            AHBottomNavigationItem(0, R.drawable.ic_tab_connections, R.color.accent),
                            AHBottomNavigationItem(0, R.drawable.ic_tab_notifications, R.color.accent),
                            AHBottomNavigationItem(0, R.drawable.ic_tab_chat, R.color.accent)
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

    override fun onSaveInstanceState(outState: Bundle) {
        var result = outState
        drawer?.apply { result = saveInstanceState(result) }
        accountHeader?.apply { result = saveInstanceState(result) }

        super.onSaveInstanceState(result)
    }

    private fun setCounter(pageIndex: Int, counterValue: Int) {
        val view = view ?: return

        val notification = if (counterValue != 0) AHNotification.Builder()
                .setText(counterValue.toString())
                .setBackgroundColor(ContextCompat.getColor(view.context, R.color.accent))
                .setTextColor(ContextCompat.getColor(view.context, R.color.white))
                .build() else null
        view.bnMain.setNotification(notification, pageIndex)
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

//        requireNotNull(view).drawerLayout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.nav_wallet -> open(WalletController.newInstance())
            R.id.nav_all_connections -> open(AllConnectionsController.newInstance())
            R.id.nav_build_network -> open(BuildNetworkController.newInstance())
            R.id.nav_change_account -> open(SelectAccountController.newInstance())
            R.id.nav_create_account -> open(RegistrationController.newInstance())
            R.id.nav_invite_to_mnassa -> open(InviteController.newInstance())
            R.id.nav_logout -> viewModel.logout()
        }

        return true
    }

    override fun handleBack(): Boolean {
        return if (drawer?.isDrawerOpen == true) {
            drawer?.closeDrawer()
            true
        } else {
            super.handleBack()
        }
    }

    override fun onDestroyView(view: View) {
        if (!requireNotNull(activity).isChangingConfigurations) {
            view.vpMain.adapter = null
        }
        drawer = null
        accountHeader = null
        super.onDestroyView(view)
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(self, controller)

    override fun close(self: Controller) = mnassaRouter.close(self)

    private fun formatTabControllerTag(position: Int): String = "tab_controller_$position"

    private enum class Pages {
        HOME, CONNECTIONS, NOTIFICATIONS, CHAT
    }

    companion object {
        fun newInstance() = MainController()
    }
}