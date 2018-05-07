package com.mnassa.screen.posts

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.bufferize
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_posts_list.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class PostsController : MnassaControllerImpl<PostsViewModel>(), OnPageSelected {
    override val layoutId: Int = R.layout.controller_posts_list
    override val viewModel: PostsViewModel by instance()
    private val adapter = PostsRVAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            adapter.restoreState(this)
        }

        //todo: send only when page selected
        adapter.onAttachedToWindow = { post -> viewModel.onAttachedToWindow(post) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = {
            launchCoroutineUI {
                if (viewModel.permissionsChannel.consume { receive() }.canCreateNeedPost) {
                    open(CreateNeedController.newInstance())
                }
            }
        }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onPostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onHideInfoPostClickListener = { viewModel.hideInfoPost(it) }

        adapter.isLoadingEnabled = savedInstanceState == null
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.newsFeedChannel.openSubscription().bufferize(controllerSubscriptionContainer).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        if (it.item.isNotEmpty()) {
                            adapter.dataStorage.addAll(it.item)
                        }
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = true
                    }
                }
            }
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.infoFeedChannel.openSubscription().bufferize(controllerSubscriptionContainer).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        if (it.item.isNotEmpty()) {
                            adapter.dataStorage.addAll(it.item)
                        }
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                }
            }
        }

        viewModel.resetCounter()

    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvNewsFeed.adapter = adapter
        }
    }

    override fun onPageSelected() {
        val recyclerView = view?.rvNewsFeed ?: return
        recyclerView.scrollToPosition(0)
        viewModel.resetCounter()
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