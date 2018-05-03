package com.mnassa.screen.home

import android.support.design.widget.TabLayout
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.EventsController
import com.mnassa.screen.events.create.CreateEventController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.posts.PostsController
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class HomeController : MnassaControllerImpl<HomeViewModel>(), MnassaRouter, OnPageSelected {
    override val layoutId: Int = R.layout.controller_home
    override val viewModel: HomeViewModel by instance()

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    HomePage.NEEDS.ordinal -> PostsController.newInstance()
                    HomePage.EVENTS.ordinal -> EventsController.newInstance()
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page).tag(formatTabControllerTag(position)))
            }
        }

        override fun getCount(): Int = HomePage.values().size

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            HomePage.NEEDS.ordinal -> fromDictionary(R.string.tab_home_posts_title)
            HomePage.EVENTS.ordinal -> fromDictionary(R.string.tab_home_events_title)
            else -> throw IllegalArgumentException("Invalid page position $position")
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpHome.adapter = adapter
            tlHome.setupWithViewPager(vpHome)
            tlHome.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(vpHome) {
                override fun onTabSelected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = onPageSelectionChanged(true)
            })

            launchCoroutineUI {
                viewModel.unreadEventsCountChannel.consumeEach {
                    tlHome.setBadgeText(HomePage.EVENTS.ordinal, it.takeIf { it > 0 }?.toString())
                }
            }
            launchCoroutineUI {
                viewModel.unreadNeedsCountChannel.consumeEach {
                    tlHome.setBadgeText(HomePage.NEEDS.ordinal, it.takeIf { it > 0 }?.toString())
                }
            }
            initFab(this)
        }
    }

    private fun initFab(view: View) {
        with(view) {
            famHome.setClosedOnTouchOutside(true)

            fabCreateNeed.labelText = fromDictionary(R.string.tab_home_button_create_need)
            fabCreateNeed.setOnClickListener {
                famHome.close(false)
                open(CreateNeedController.newInstance())
            }

            fabCreateOffer.labelText = fromDictionary(R.string.tab_home_button_create_offer)
            fabCreateOffer.setOnClickListener {
                famHome.close(false)
                open(CreateOfferController.newInstance())
            }

            fabCreateEvent.labelText = fromDictionary(R.string.tab_home_button_create_event)
            fabCreateEvent.setOnClickListener {
                famHome.close(false)
                open(CreateEventController.newInstance())
            }

            fabCreateGeneralPost.labelText = fromDictionary(R.string.tab_home_button_create_general_post)
            fabCreateGeneralPost.setOnClickListener {
                famHome.close(false)
                open(CreateGeneralPostController.newInstance())
            }
        }

        launchCoroutineUI {
            viewModel.permissionsChannel.consumeEach { permission ->
                with(getViewSuspend()) {
                    fabCreateNeed.isEnabled = permission.canCreateNeedPost
                    fabCreateOffer.isEnabled = permission.canCreateOfferPost
                    fabCreateEvent.isEnabled = permission.canCreateEvent
                    fabCreateGeneralPost.isEnabled = permission.canCreateGeneralPost
                    famHome.isEnabled = (
                            permission.canCreateNeedPost ||
                                    permission.canCreateOfferPost ||
                                    permission.canCreateEvent ||
                                    permission.canCreateGeneralPost)
                }
            }
        }
    }

    override fun onPageSelected() = onPageSelectionChanged(true)
    override fun onPageUnSelected() = onPageSelectionChanged(false)

    private fun onPageSelectionChanged(isSelected: Boolean) {
        val view = view ?: return
        val selectedPageIndex = view.vpHome.currentItem
        val controller = adapter.getRouter(selectedPageIndex)?.getControllerWithTag(formatTabControllerTag(selectedPageIndex))
        if (controller is OnPageSelected) {
            if (isSelected) controller.onPageSelected()
            else controller.onPageUnSelected()
        }
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(this, controller)
    override fun close(self: Controller) = mnassaRouter.close(self)

    private fun formatTabControllerTag(position: Int): String = "home_tab_controller_$position"

    enum class HomePage {
        NEEDS, EVENTS
    }

    companion object {
        fun newInstance() = HomeController()
    }
}