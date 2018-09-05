package com.mnassa.screen.main

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.MnassaAccountHeaderBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.holder.DimenHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mnassa.R
import com.mnassa.activity.SecondActivity
import com.mnassa.core.addons.asReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.ConnectionsController
import com.mnassa.screen.deeplink.DeeplinkHandler
import com.mnassa.screen.group.list.GroupListController
import com.mnassa.screen.home.HomeController
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import com.mnassa.screen.main.MainController.DrawerItem.*
import com.mnassa.screen.notifications.NotificationsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.screen.settings.SettingsController
import com.mnassa.screen.termsandconditions.TermsAndConditionsController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaProfileDrawerItem
import kotlinx.android.synthetic.main.controller_main.view.*
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>(), MnassaRouter, PageContainer {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()
    private var drawer: Drawer? = null
    private var accountHeader: AccountHeader? = null
    private var activeAccount: ShortAccountModel? = null
    private var previousSelectedPage = 0
    private val deeplinkHandler: DeeplinkHandler by instance()

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

    private fun updateActiveProfile(fireOnProfileChanged: Boolean = false) {
        accountHeader?.apply {
            profiles.forEach {
                if ((it as? MnassaProfileDrawerItem)?.account?.id == activeAccount?.id) {
                    setActiveProfile(it, fireOnProfileChanged)
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpMain.adapter = adapter

            accountHeader = MnassaAccountHeaderBuilder()
                    .withActivity(requireNotNull(activity))
                    .withTranslucentStatusBar(true)
                    .withAccountHeader(R.layout.drawer_header)
                    .withOnAccountHeaderListener { _: View, profile: IProfile<Any>, _: Boolean ->
                        val innerProfile = profile as IProfile<ProfileDrawerItem>
                        when {
                            innerProfile is MnassaProfileDrawerItem -> {
                                val account = innerProfile.account
                                drawer?.closeDrawer()
                                activeAccount = account
                                viewModel.selectAccount(account)
                                true
                            }
                            profile.identifier == ACCOUNT_ADD -> {
                                post { open(RegistrationController.newInstance()) }
                                false
                            }
                            else -> false
                        }
                    }
                    .build()

            val appInfoProvider: AppInfoProvider by instance()

            drawer = DrawerBuilder(requireNotNull(activity))
                    .withRootView(root)
                    .withAccountHeader(requireNotNull(accountHeader))
                    .addDrawerItems(
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_profile)).withIcon(R.drawable.ic_profile).withIdentifier(PROFILE.ordinal.toLong()).withSelectable(false),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_groups)).withIcon(R.drawable.ic_group).withIdentifier(GROUPS.ordinal.toLong()).withSelectable(false)
                                    .withBadge(null as String?)
                                    .withBadgeStyle(BadgeStyle().withTextColorRes(R.color.white).withColorRes(R.color.accent).withCornersDp(12).withPadding(DimenHolder.fromDp(2))),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_wallet)).withIcon(R.drawable.ic_wallet).withIdentifier(WALLET.ordinal.toLong()).withSelectable(false),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_invite)).withIcon(R.drawable.ic_invite).withIdentifier(INVITE.ordinal.toLong()).withSelectable(false),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_settings)).withIcon(R.drawable.ic_settings).withIdentifier(SETTINGS.ordinal.toLong()).withSelectable(false),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_help)).withIcon(R.drawable.ic_support).withIdentifier(HELP.ordinal.toLong()).withSelectable(false),
                            PrimaryDrawerItem().withName(fromDictionary(R.string.side_menu_terms)).withIcon(R.drawable.ic_terms).withIdentifier(TERMS.ordinal.toLong()).withSelectable(false),
                            DividerDrawerItem(),
                            SecondaryDrawerItem().withName(fromDictionary(R.string.side_menu_logout)).withIdentifier(LOGOUT.ordinal.toLong()).withSelectable(false),
                            SecondaryDrawerItem().withName("${appInfoProvider.appName} ${appInfoProvider.versionName}").withEnabled(false).withSelectable(false)
                    )
                    .withSelectedItem(-1)
                    .withOnDrawerItemClickListener { _, _, item ->
                        when (values()[item.identifier.toInt()]) {
                            PROFILE -> activeAccount?.let { open(ProfileController.newInstance(it)) }
                            GROUPS -> open(GroupListController.newInstance())
                            WALLET -> open(WalletController.newInstance())
                            INVITE -> {
                                requireNotNull(applicationContext).getInstance<InviteSourceHolder>().source = InviteSource.Manual()
                                open(InviteController.newInstance())
                            }
                            SETTINGS -> open(SettingsController.newInstance())
                            HELP -> open(ChatMessageController.newInstance())
                            TERMS -> open(TermsAndConditionsController.newInstance())
                            LOGOUT -> viewModel.logout()
                        }
                        true
                    }
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
                Pages.values().forEach { page ->
                    val index = page.ordinal
                    val controller = adapter.getRouter(index)?.getControllerWithTag(formatTabControllerTag(index))
                    if (controller is OnPageSelected) {
                        if (index == position) controller.onPageSelected()
                        else controller.onPageUnSelected()
                    }
                    if (controller is OnScrollToTop) {
                        controller.scrollToTop()
                    }
                }

                if (previousSelectedPage == Pages.NOTIFICATIONS.ordinal) {
                    viewModel.resetAllNotifications()
                }
                previousSelectedPage = position
                true
            }
        }

        launchCoroutineUI {
            viewModel.unreadChatsCountChannel.consumeEach { setCounter(Pages.CHAT, it) }
        }

        launchCoroutineUI {
            viewModel.unreadNotificationsCountChannel.consumeEach { setCounter(Pages.NOTIFICATIONS, it) }
        }

        launchCoroutineUI {
            viewModel.unreadConnectionsCountChannel.consumeEach { setCounter(Pages.CONNECTIONS, it) }
        }

        launchCoroutineUI {
            viewModel.unreadEventsAndNeedsCountChannel.consumeEach { setCounter(Pages.HOME, it) }
        }

        launchCoroutineUI {
            viewModel.availableAccountsChannel.consumeEach { accounts ->
                accountHeader?.apply {
                    clear()
                    accounts.forEach { account ->
                        addProfile(MnassaProfileDrawerItem().withAccount(account), profiles.size)
                    }
                    addProfile(ProfileSettingDrawerItem()
                            .withName(fromDictionary(R.string.side_menu_add_account))
                            .withIcon(R.drawable.ic_add_black_24dp)
                            .withIdentifier(ACCOUNT_ADD)
                            .withSelectable(false), profiles.size)
                    updateActiveProfile()
                }
            }
        }

        launchCoroutineUI {
            viewModel.currentAccountChannel.consumeEach {
                activeAccount = it
                accountHeader?.setActiveProfile(it.id.hashCode().toLong())
            }
        }

        launchCoroutineUI {
            viewModel.groupInvitesCountChannel.consumeEach { groupsCount ->
                drawer?.let {
                    val text = when (groupsCount){
                        0 -> null
                        in 1..9 -> "  $groupsCount  "
                        in 10..99 -> " $groupsCount "
                        else -> groupsCount.toString()
                    }
                    it.updateBadge(GROUPS.ordinal.toLong(), StringHolder(text))
                }
            }
        }

        activity?.intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: Intent) {
        if (deeplinkHandler.hasDeeplink(intent)) showProgress()
        else return
        launchCoroutineUI(CoroutineStart.UNDISPATCHED) {
            deeplinkHandler.handle(intent)?.let { controller ->
                open(controller)
                getViewSuspend().bnMain.currentItem = Pages.NOTIFICATIONS.ordinal
            }
        }.invokeOnCompletion { hideProgress() }
    }

    override fun isPageSelected(page: Controller): Boolean {
        val currentPageIndex = view?.vpMain?.currentItem ?: return false

        val controllerPage = when (page) {
            is HomeController -> Pages.HOME.ordinal
            is ConnectionsController -> Pages.CONNECTIONS.ordinal
            is NotificationsController -> Pages.NOTIFICATIONS.ordinal
            is ChatListController -> Pages.CHAT.ordinal
            else -> -1
        }

        return currentPageIndex == controllerPage
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var result = outState
        drawer?.apply { result = saveInstanceState(result) }
        accountHeader?.apply { result = saveInstanceState(result) }

        super.onSaveInstanceState(result)
    }

    private fun setCounter(page: Pages, counterValue: Int) {
        val view = view ?: return

        val notification = if (counterValue != 0) AHNotification.Builder()
                .setText(counterValue.toString())
                .setBackgroundColor(ContextCompat.getColor(view.context, R.color.accent))
                .setTextColor(ContextCompat.getColor(view.context, R.color.white))
                .build() else null
        view.bnMain.setNotification(notification, page.ordinal)
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
        HOME,
        CONNECTIONS,
        NOTIFICATIONS,
        CHAT
    }

    enum class DrawerItem {
        PROFILE,
        GROUPS,
        WALLET,
        INVITE,
        SETTINGS,
        HELP,
        TERMS,
        LOGOUT
    }

    companion object {
        const val ACCOUNT_ADD = -998L

        fun newInstance() = MainController()
    }


}