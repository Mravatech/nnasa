package com.mnassa.screen.posts

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.PostCountersImpl
import com.mnassa.domain.model.impl.PostModelImpl
import com.mnassa.domain.repository.UserRepository
import com.mnassa.extensions.firstVisibleItemPosition
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_posts_list.view.*
import kotlinx.android.synthetic.main.new_items_panel.view.*
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import org.kodein.di.generic.instance
import java.util.*
import kotlin.math.abs

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
        adapter.onDetachedFromWindow = { post -> controllerSelectedExecutor.invoke { viewModel.onDetachedFromWindow(post) } }
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
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            subscribeToUpdates(viewModel.newsFeedChannel)
        }
        controllerSubscriptionContainer.launchCoroutineUI {
            subscribeToUpdates(viewModel.newsFeedUpdatesChannel)
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
    }

    private fun getFirstItem(): PostModel? {
        if (adapter.dataStorage.isEmpty()) return null
        return adapter.dataStorage[0]
    }

    private suspend fun subscribeToUpdates(channel: BroadcastChannel<ListItemEvent<List<PostModel>>>) {
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
        view.rvNewsFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

       launchCoroutineUI {
           (1..1000).forEach {
               delay(5_000)
               val post = PostModelImpl(
                       "aaaa$it",
                       true,
                       PostType.NEED(),
                       Date(),
                       emptyList(),
                       null,
                       Date(),
                       "aaaa$it",
                       emptySet(),
                       PostPrivacyType.PUBLIC(),
                       emptyList(),
                       "Test $it",
                       null, null, Date(), PostCountersImpl(it, it, it, it, it, it),
                       view.context.getInstance<UserRepository>().getCurrentAccountOrException(),
                       null,
                       0.0,
                       PostAutoSuggest.EMPTY,
                       null,
                       emptySet(),
                       emptyList()
               )
               viewModel.newsFeedUpdatesChannel.send(ListItemEvent.Added(listOf(post)))
           }
       }
    }

    private fun triggerScrollPanel() {
        view?.rvNewsFeed?.scrollBy(0, 0)
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
        private const val PANEL_ANIMATION_DURATION = 500L
        private const val PANEL_ANIMATION_START_POSITION = 0f
        private const val PANEL_ANIMATION_START_ALPHA = 1f
        private const val PANEL_ANIMATION_END_POSITION = -100f
        private const val PANEL_ANIMATION_END_ALPHA = 0f
    }
}