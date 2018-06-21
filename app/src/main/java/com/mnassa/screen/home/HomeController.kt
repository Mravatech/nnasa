package com.mnassa.screen.home

import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import com.github.clans.fab.FloatingActionButton
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.AccountType
import com.mnassa.extensions.isGone
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.EventsController
import com.mnassa.screen.events.create.CreateEventController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.posts.PostsController
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.screen.profile.edit.company.EditCompanyProfileController
import com.mnassa.screen.profile.edit.personal.EditPersonalProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_home.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class HomeController : MnassaControllerImpl<HomeViewModel>(), MnassaRouter, OnPageSelected, PageContainer, OnScrollToTop {
    override val layoutId: Int = R.layout.controller_home
    override val viewModel: HomeViewModel by instance()
    private val dialogHelper: DialogHelper by instance()

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
                override fun onTabSelected(tab: TabLayout.Tab?) = onPageSelectionChanged(true, false)
                override fun onTabReselected(tab: TabLayout.Tab?) = onPageSelectionChanged(true, true)
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

            launchCoroutineUI {
                viewModel.showAddTagsDialog.consumeEach {
                    dialogHelper.showAddTagsDialog(getViewSuspend().context) {
                        launchCoroutineUI {
                            val offers = viewModel.getOffers()
                            val interests = viewModel.getInterests()
                            val profileModel = viewModel.getProfile() ?: return@launchCoroutineUI

                            open(when (profileModel.accountType) {
                                AccountType.PERSONAL -> EditPersonalProfileController.newInstance(profileModel, offers, interests)
                                AccountType.ORGANIZATION -> EditCompanyProfileController.newInstance(profileModel, offers, interests)
                            })
                        }
                    }
                }
            }

            initFab(this)
        }
    }

    override fun isPageSelected(page: Controller): Boolean {
        val isThisControllerSelected = (parentController as? PageContainer)?.isPageSelected(this)
        return if (isThisControllerSelected != false) {
            val currentPageIndex = view?.vpHome?.currentItem ?: return false
            val controllerPage = when(page) {
                is PostsController -> HomePage.NEEDS.ordinal
                is EventsController -> HomePage.EVENTS.ordinal
                else -> -1
            }
            currentPageIndex == controllerPage
        } else false
    }

    private fun initFab(view: View) {
        with(view) {
            famHome.setClosedOnTouchOutside(true)
        }

        launchCoroutineUI {
            viewModel.permissionsChannel.consumeEach { permission ->
                with(getViewSuspend()) {
                    famHome.removeAllMenuButtons()

                    if (permission.canCreateGeneralPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_general_post))
                        button.setOnClickListener {
                            famHome.close(false)
                            open(CreateGeneralPostController.newInstance())
                        }
                        famHome.addMenuButton(button)
                    }

                    if (permission.canCreateEvent) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_event))
                        button.setOnClickListener {
                            famHome.close(false)
                            open(CreateEventController.newInstance())
                        }
                        famHome.addMenuButton(button)
                    }

                    if (permission.canCreateOfferPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_offer))
                        button.setOnClickListener {
                            famHome.close(false)
                            open(CreateOfferController.newInstance())
                        }
                        famHome.addMenuButton(button)
                    }

                    if (permission.canCreateNeedPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_need))
                        button.setOnClickListener {
                            famHome.close(false)
                            open(CreateNeedController.newInstance())
                        }
                        famHome.addMenuButton(button)
                    }

                    famHome.isGone = !(
                            permission.canCreateNeedPost ||
                                    permission.canCreateOfferPost ||
                                    permission.canCreateEvent ||
                                    permission.canCreateGeneralPost)
                }
            }
        }
    }

    private fun View.inflateMenuButton(text: String): FloatingActionButton {
        val button = FloatingActionButton(context)
        button.buttonSize = FloatingActionButton.SIZE_MINI
        button.setImageResource(R.drawable.ic_edit_white_24dp)
        button.colorNormal = ContextCompat.getColor(context, R.color.accent)
        button.colorPressed = ContextCompat.getColor(context, R.color.tealish)
        button.labelText = text

        return button
    }

    override fun onPageSelected() = onPageSelectionChanged(true, false)
    override fun onPageUnSelected() = onPageSelectionChanged(false, false)
    override fun scrollToTop() = onPageSelectionChanged(true, true)

    private fun onPageSelectionChanged(isSelected: Boolean, isReSelected: Boolean) {
        val view = view ?: return
        val selectedPageIndex = view.vpHome.currentItem
        val controller = adapter.getRouter(selectedPageIndex)?.getControllerWithTag(formatTabControllerTag(selectedPageIndex))
        if (controller is OnPageSelected) {
            if (isSelected) controller.onPageSelected()
            else controller.onPageUnSelected()
        }
        if (isReSelected && controller is OnScrollToTop) {
            controller.scrollToTop()
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