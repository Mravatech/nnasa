package com.mnassa.screen.needs

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.isNewItemsNeeded
import com.mnassa.extensions.waitForNewItems
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_needs_list.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEachIndexed
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 3/6/2018.
 */
class NeedsController : MnassaControllerImpl<NeedsViewModel>() {
    override val layoutId: Int = R.layout.controller_needs_list
    override val viewModel: NeedsViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            rvNewsFeed.layoutManager = LinearLayoutManager(context)
            srlNewsFeed.setColorSchemeResources(R.color.colorAccent)
            srlNewsFeed.setOnRefreshListener {
                startPagination()
            }
        }

        startPagination()
    }

    private var paginationJob: Job? = null
    private fun startPagination() {
        paginationJob?.cancel()
        val recyclerView = view?.rvNewsFeed ?: return
        val srlNewsFeed = view?.srlNewsFeed

        val adapter: NewsFeedRVAdapter = if (recyclerView.adapter is NewsFeedRVAdapter) {
            recyclerView.adapter as NewsFeedRVAdapter
        } else {
            val localAdapter = NewsFeedRVAdapter()
            recyclerView.adapter = localAdapter
            localAdapter
        }

        if (srlNewsFeed?.isRefreshing == false) {
            adapter.isLoadingEnabled = true
        }
        var atLeastOneItemAdded = false
        paginationJob = launchCoroutineUI {
            viewModel.getNewsFeedChannel().consumeEachIndexed {
                delay(1500)
                Timber.i("NewsFeedPagination -> loaded ${it.index}")
                adapter.dataStorage.add(it.value)
                atLeastOneItemAdded = true

                if (recyclerView.isNewItemsNeeded(adapter.emptyItemCount)) {
                    if (srlNewsFeed?.isRefreshing == false) {
                        adapter.isLoadingEnabled = true
                    }
                } else {
                    srlNewsFeed?.isRefreshing = false
                    adapter.isLoadingEnabled = false
                    recyclerView.waitForNewItems(adapter.emptyItemCount)
                }
            }
        }

        paginationJob?.invokeOnCompletion {
            adapter.isLoadingEnabled = false
            view?.rvEmpty?.visibility = if (atLeastOneItemAdded) View.INVISIBLE else View.VISIBLE
            Timber.i("NewsFeedPagination -> ###DONE###")
        }
    }


    companion object {
        fun newInstance() = NeedsController()
    }
}