package com.mnassa.screen.home

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.EventsController
import com.mnassa.screen.needs.NeedsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class HomeController : MnassaControllerImpl<HomeViewModel>() {
    override val layoutId: Int = R.layout.controller_home
    override val viewModel: HomeViewModel by instance()

    private val adapter: RouterPagerAdapter = object : RouterPagerAdapter(this) {
        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val page: Controller = when (position) {
                    PAGE_NEEDS -> NeedsController.newInstance()
                    PAGE_EVENTS -> EventsController.newInstance()
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = PAGES_COUNT

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            PAGE_NEEDS -> fromDictionary(R.string.tab_home_needs_title)
            PAGE_EVENTS -> fromDictionary(R.string.tab_home_events_title)
            else -> throw IllegalArgumentException("Invalid page position $position")
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            vpHome.adapter = adapter
            tlHome.setupWithViewPager(vpHome)

            launchCoroutineUI {
                viewModel.unreadEventsCountChannel.consumeEach {
                    tlHome.setBadgeText(PAGE_EVENTS, if (it == 0) null else it.toString())
                }
            }
            launchCoroutineUI {
                viewModel.unreadNeedsCountChannel.consumeEach {
                    tlHome.setBadgeText(PAGE_NEEDS, if (it == 0) null else it.toString())
                }
            }
        }
    }


    companion object {
        private const val PAGES_COUNT = 2
        private const val PAGE_NEEDS = 0
        private const val PAGE_EVENTS = 1

        fun newInstance() = HomeController()
    }
}