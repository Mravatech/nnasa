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
import com.mnassa.screen.posts.PostsController
import com.mnassa.screen.posts.need.create.CreateNeedController
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
                    HomePage.NEEDS.ordinal -> PostsController.newInstance()
                    HomePage.EVENTS.ordinal -> EventsController.newInstance()
                    else -> throw IllegalArgumentException("Invalid page position $position")
                }
                router.setRoot(RouterTransaction.with(page))
            }
        }

        override fun getCount(): Int = HomePage.values().size

        override fun getPageTitle(position: Int): CharSequence = when (position) {
            HomePage.NEEDS.ordinal -> fromDictionary(R.string.tab_home_needs_title)
            HomePage.EVENTS.ordinal -> fromDictionary(R.string.tab_home_events_title)
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
                    tlHome.setBadgeText(HomePage.EVENTS.ordinal, if (it == 0) null else it.toString())
                }
            }
            launchCoroutineUI {
                viewModel.unreadNeedsCountChannel.consumeEach {
                    tlHome.setBadgeText(HomePage.NEEDS.ordinal, if (it == 0) null else it.toString())
                }
            }

            fabCreateNeed.labelText = fromDictionary(R.string.tab_home_button_create_need)
            fabCreateNeed.setOnClickListener { open(CreateNeedController.newInstance()) }

            fabCreateOffer.labelText = fromDictionary(R.string.tab_home_button_create_offer)
        }
    }


    enum class HomePage {
        NEEDS, EVENTS
    }

    companion object {
        fun newInstance() = HomeController()
    }
}