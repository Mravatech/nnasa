package com.mnassa.screen.events

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.bufferize
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.markAsOpened
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_events_list.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 3/6/2018.
 */
class EventsController : MnassaControllerImpl<EventsViewModel>(), OnPageSelected {
    override val layoutId: Int = R.layout.controller_events_list
    override val viewModel: EventsViewModel by instance()
    private val languageProvider: LanguageProvider by instance()
    private val userInteractor: UserProfileInteractor by instance()
    private val adapter by lazy { EventsRVAdapter(languageProvider, userInteractor) }

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        savedInstanceState?.apply { adapter.restoreState(this) }

        adapter.onAttachedToWindow = { viewModel.onAttachedToWindow(it) }
        adapter.onAuthorClickListener = { open(ProfileController.newInstance(it.author)) }
        adapter.onItemClickListener = { openEvent(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)

        view.rvEvents.adapter = adapter

        adapter.isLoadingEnabled = savedInstanceState == null
        launchCoroutineUI {
            viewModel.eventsFeedChannel.openSubscription().bufferize(this@EventsController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false

                        if (it.item.isNotEmpty()) {
                            adapter.dataStorage.addAll(it.item)
                            getViewSuspend().rlEmptyView.isInvisible = true
                        } else {
                            getViewSuspend().rlEmptyView.isInvisible = !adapter.dataStorage.isEmpty()
                        }
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = true
                        getViewSuspend().rlEmptyView.isInvisible = true
                    }
                }
            }
        }

        viewModel.resetCounter()
    }

    override fun onPageSelected() {
        val recyclerView = view?.rvEvents ?: return
        recyclerView.scrollToPosition(0)
        viewModel.resetCounter()
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
    }
}