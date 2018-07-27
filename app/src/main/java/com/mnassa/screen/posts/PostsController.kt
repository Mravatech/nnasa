package com.mnassa.screen.posts

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_posts_list.view.*
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class PostsController : MnassaControllerImpl<PostsViewModel>(), OnPageSelected, OnScrollToTop {
    override val layoutId: Int = R.layout.controller_posts_list
    override val viewModel: PostsViewModel by instance()
    private val adapter = PostsRVAdapter()
    private val controllerSelectedExecutor = StateExecutor<Unit, Unit>(initState = Unit) {
        val parent = parentController
        parent is PageContainer && parent.isPageSelected(this@PostsController)
    }
    private var lastViewedPostDate: Long = -1
    private var hasNewPosts: Boolean = false
        get() {
            return lastViewedPostDate < getFirstItem()?.createdAt?.time ?: -1
        }
    private var postIdToScroll: String? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            adapter.restoreState(this)
        }

        adapter.onAttachedToWindow = { post ->
            controllerSelectedExecutor.invoke { viewModel.onAttachedToWindow(post) }
            if (post.createdAt.time > lastViewedPostDate) {
                lastViewedPostDate = post.createdAt.time
            }
        }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = {
            controllerSubscriptionContainer.launchCoroutineUI {
                if (viewModel.permissionsChannel.consume { receive() }.canCreateNeedPost) {
                    open(CreateNeedController.newInstance())
                }
            }
        }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onPostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onHideInfoPostClickListener = viewModel::hideInfoPost
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled

            if (postIdToScroll != null) {
                val dataIndex = adapter.dataStorage.indexOfFirst { it.id == postIdToScroll }
                if (dataIndex >= 0) {
                    postIdToScroll = null
                    val layoutManager = view?.rvNewsFeed?.layoutManager
                    layoutManager as LinearLayoutManager
                    layoutManager.scrollToPosition(adapter.convertDataIndexToAdapterPosition(dataIndex))
                }
            }
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            subscribeToUpdates(viewModel.newsFeedChannel.openSubscription())
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.infoFeedChannel.openSubscription().consumeEach {
                when (it) {
                    is ListItemEvent.Added -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Changed -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.remove(it.item)
                }
            }
        }

        //scroll to element logic
        lifecycle.subscribe {
            if (it == Lifecycle.Event.ON_PAUSE) {
                val layoutManager = view?.rvNewsFeed?.layoutManager ?: return@subscribe
                layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstVisibleDataPosition = maxOf(0, adapter.convertAdapterPositionToDataIndex(firstVisiblePosition))
                if (adapter.dataStorage.isEmpty()) return@subscribe
                viewModel.saveScrollPosition(adapter.dataStorage[firstVisibleDataPosition])
            }
        }

        postIdToScroll = viewModel.restoreScrollPosition()
        viewModel.resetScrollPosition()
    }

    private fun getFirstItem(): PostModel? {
        if (adapter.dataStorage.isEmpty()) return null
        return adapter.dataStorage[0]
    }

    private suspend fun subscribeToUpdates(channel: ReceiveChannel<ListItemEvent<List<PostModel>>>) {
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

        view.rvNewsFeed.adapter = adapter
        view.rvNewsFeed.attachPanel { hasNewPosts }
    }

    private fun triggerScrollPanel() {
        view?.rvNewsFeed?.scrollBy(0, 0)
    }

    override fun scrollToTop() {
        val recyclerView = view?.rvNewsFeed ?: return
        recyclerView.scrollToPosition(0)
    }

    override fun onPageSelected() {
        controllerSelectedExecutor.trigger()
    }

    override fun onDestroyView(view: View) {
        view.rvNewsFeed.adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    companion object {
        fun newInstance() = PostsController()
    }
}