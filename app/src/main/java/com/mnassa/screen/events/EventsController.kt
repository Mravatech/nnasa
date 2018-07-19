package com.mnassa.screen.events

import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import androidx.content.edit
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.firstVisibleItemPosition
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.markAsOpened
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_events_list.view.*
import kotlinx.android.synthetic.main.new_items_panel.view.*
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.kodein.di.generic.instance
import timber.log.Timber
import kotlin.math.abs

/**
 * Created by Peter on 3/6/2018.
 */
class EventsController : MnassaControllerImpl<EventsViewModel>(), OnPageSelected, OnScrollToTop {
    override val layoutId: Int = R.layout.controller_events_list
    override val viewModel: EventsViewModel by instance()
    private val languageProvider: LanguageProvider by instance()
    private val userInteractor: UserProfileInteractor by instance()
    private val adapter by lazy { EventsRVAdapter(languageProvider, userInteractor) }
    private val controllerSelectedExecutor = StateExecutor<Unit, Unit>(initState = Unit) {
        val parent = parentController
        parent is PageContainer && parent.isPageSelected(this@EventsController)
    }
    private var lastViewedPostDate: Long = -1
    private var hasNewPosts: Boolean = false
        get() {
            return lastViewedPostDate < getFirstItem()?.createdAt?.time ?: -1
        }
    private var postIdToScroll: String? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        savedInstanceState?.apply { adapter.restoreState(this) }

        adapter.onAttachedToWindow = { post ->
            controllerSelectedExecutor.invoke { viewModel.onAttachedToWindow(post) }
            if (post.createdAt.time > lastViewedPostDate) {
                lastViewedPostDate = post.createdAt.time
            }
        }
        adapter.onAuthorClickListener = { open(ProfileController.newInstance(it.author)) }
        adapter.onItemClickListener = { openEvent(it) }
        adapter.isLoadingEnabled = savedInstanceState == null

        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled

            if (postIdToScroll != null) {
                val dataIndex = adapter.dataStorage.indexOfFirst { it.id == postIdToScroll }
                if (dataIndex >= 0) {
                    postIdToScroll = null
                    val layoutManager = view?.rvEvents?.layoutManager
                    layoutManager as LinearLayoutManager
                    layoutManager.scrollToPosition(adapter.convertDataIndexToAdapterPosition(dataIndex))
                }
            }
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            subscribeToUpdates(viewModel.eventsFeedChannel)
        }

        //scroll to element logic

        val sharedPrefs = requireNotNull(applicationContext).getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        lifecycle.subscribe {
            if (it == Lifecycle.Event.ON_PAUSE) {
                val layoutManager = view?.rvEvents?.layoutManager ?: return@subscribe
                layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstVisibleDataPosition = maxOf(0, adapter.convertAdapterPositionToDataIndex(firstVisiblePosition))
                if (adapter.dataStorage.isEmpty()) return@subscribe
                val firstItem = adapter.dataStorage[firstVisibleDataPosition]

                sharedPrefs.edit {
                    putString(PREFS_FIRST_EVENT_ID, firstItem.id)
                }
            }
        }

        postIdToScroll = sharedPrefs.getString(PREFS_FIRST_EVENT_ID, null).also {
            sharedPrefs.edit { clear() }
        }
    }

    private suspend fun subscribeToUpdates(channel: BroadcastChannel<ListItemEvent<List<EventModel>>>) {
        channel.consumeEach {
            when (it) {
                is ListItemEvent.Added -> {
                    adapter.isLoadingEnabled = false
                    adapter.dataStorage.addAll(it.item)
                    triggerScrollPanel()
                }
                is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                is ListItemEvent.Cleared -> {
                    adapter.isLoadingEnabled = true
                    adapter.dataStorage.clear()
                    lastViewedPostDate = -1
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.rvEvents.adapter = adapter
        view.rvEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isShown = false
            private var isHidden = false

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 && abs(dy) < 10) return

                if (/*dy > 0 && */recyclerView.firstVisibleItemPosition > 1 && hasNewPosts) {
                    if (isShown) return
                    showNewItemsPanel()
                    isShown = true
                    isHidden = false
                } else {
                    if (isHidden) return
                    hideNewItemsPanel()
                    isHidden = true
                    isShown = false
                }
            }
        })
        view.flNewItemsPanel.animate().alpha(PANEL_ANIMATION_END_ALPHA).setDuration(0L).start()
        view.flNewItemsPanel.setOnClickListener { scrollToTop() }
    }

    private fun triggerScrollPanel() {
        view?.rvEvents?.scrollBy(0, 0)
    }

    private fun showNewItemsPanel() {

        val panel = view?.flNewItemsPanel ?: return
        panel.animate()
                .setDuration(PANEL_ANIMATION_DURATION)
                .translationY(PANEL_ANIMATION_START_POSITION)
                .alpha(PANEL_ANIMATION_START_ALPHA)
                .start()
    }

    private fun hideNewItemsPanel() {

        val panel = view?.flNewItemsPanel ?: return
        panel.animate()
                .setDuration(PANEL_ANIMATION_DURATION)
                .translationY(PANEL_ANIMATION_END_POSITION)
                .alpha(PANEL_ANIMATION_END_ALPHA)
                .start()
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
        launch {
            try {
                event.markAsOpened()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        open(EventDetailsController.newInstance(event))
    }

    companion object {
        fun newInstance() = EventsController()

        private const val PANEL_ANIMATION_DURATION = 500L
        private const val PANEL_ANIMATION_START_POSITION = 0f
        private const val PANEL_ANIMATION_START_ALPHA = 1f
        private const val PANEL_ANIMATION_END_POSITION = -100f
        private const val PANEL_ANIMATION_END_ALPHA = 0f

        private const val PREFS_FIRST_EVENT_ID = "PREFS_FIRST_EVENT_ID"
        private const val PREFS = "EVENTS_PREFS"
    }
}