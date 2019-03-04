package com.mnassa.screen.events

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.markAsOpened
import com.mnassa.extensions.subscribeToUpdates
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.group.profile.GroupProfileController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.posts.attachPanel
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_events_list.view.*
import kotlinx.android.synthetic.main.new_items_panel.view.*
import kotlinx.coroutines.GlobalScope
import org.kodein.di.generic.instance
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
class EventsController : MnassaControllerImpl<EventsViewModel>(), OnPageSelected, OnScrollToTop {
    override val layoutId: Int = R.layout.controller_events_list
    override val viewModel: EventsViewModel by instance()
    private val userInteractor: UserProfileInteractor by instance()
    private val adapter by lazy { EventsRVAdapter(this@EventsController, userInteractor) }
    private val controllerSelectedExecutor = StateExecutor<Unit, Unit>(initState = Unit) {
        val parent = parentController
        parent is PageContainer && parent.isPageSelected(this@EventsController)
    }
    private var lastViewedPostDate: Date?
        get() = viewModel.getLastViewedEventDate()
        set(value) = viewModel.setLastViewedEventDate(value)
    private var hasNewPosts: Boolean = false
        get() {
            val firstVisibleItem = getFirstItem()?.createdAt ?: return false
            val lastViewedItem = lastViewedPostDate ?: return true
            return firstVisibleItem > lastViewedItem
        }
    private var postIdToScroll: String? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        savedInstanceState?.apply { adapter.restoreState(this) }

        adapter.onAttachedToWindow = { post ->
            controllerSelectedExecutor.invoke { viewModel.onAttachedToWindow(post) }
            if (lastViewedPostDate == null || post.createdAt > lastViewedPostDate) {
                lastViewedPostDate = post.createdAt
            }
        }
        adapter.onAuthorClickListener = { open(ProfileController.newInstance(it.author)) }
        adapter.onItemClickListener = { openEvent(it) }
        adapter.onGroupClickListener = { open(GroupProfileController.newInstance(it)) }

        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled

            if (postIdToScroll != null) {
                val dataIndex = adapter.dataStorage.indexOfFirst { it.id == postIdToScroll }
                if (dataIndex >= 0) {
                    postIdToScroll = null
                    val layoutManager = view?.rvEvents?.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        layoutManager.scrollToPosition(adapter.convertDataIndexToAdapterPosition(dataIndex))
                    }
                }
            }
        }

        launchCoroutineUI {
            viewModel.eventsFeedChannel.subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().rlEmptyView },
                    onAdded = { triggerScrollPanel() },
                    onCleared = { lastViewedPostDate = null }
            )
        }

        //scroll to element logic
        lifecycle.subscribe {
            if (it == Lifecycle.Event.ON_PAUSE) {
                val layoutManager = view?.rvEvents?.layoutManager ?: return@subscribe
                layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstVisibleDataPosition = maxOf(0, adapter.convertAdapterPositionToDataIndex(firstVisiblePosition))
                if (adapter.dataStorage.isEmpty()) return@subscribe
                val firstItem = adapter.dataStorage[firstVisibleDataPosition]

                viewModel.saveScrollPosition(firstItem)
            }
        }

        postIdToScroll = viewModel.restoreScrollPosition()
        viewModel.resetScrollPosition()
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.rvEvents.adapter = adapter
        view.rvEvents.attachPanel { hasNewPosts }
        view.tvNewItemsAvailable.text = fromDictionary(view.context.getString(R.string.events_new_items_available))

        view.rvEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                viewModel.onScroll(visibleItemCount, totalItemCount, firstVisibleItemPosition)
            }
        })
    }

    private fun triggerScrollPanel() {
        view?.rvEvents?.scrollBy(0, 0)
    }

    private fun getFirstItem(): EventModel? {
        if (adapter.dataStorage.isEmpty()) return null
        return adapter.dataStorage[0]
    }

    override fun scrollToTop() {
        val recyclerView = view?.rvEvents ?: return
        recyclerView.scrollToPosition(0)
    }

    override fun onPageSelected() {
        controllerSelectedExecutor.trigger()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onDestroyView(view: View) {
        view.rvEvents.adapter = null
        super.onDestroyView(view)
    }

    private fun openEvent(event: EventModel) {
        GlobalScope.launchWorker {
            event.markAsOpened()
        }

        open(EventDetailsController.newInstance(event))
    }

    companion object {
        fun newInstance() = EventsController()
    }
}