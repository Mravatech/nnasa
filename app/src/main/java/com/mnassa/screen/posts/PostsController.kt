package com.mnassa.screen.posts

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.bufferize
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.PostDetailsController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_posts_list.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class PostsController : MnassaControllerImpl<PostsViewModel>() {
    override val layoutId: Int = R.layout.controller_posts_list
    override val viewModel: PostsViewModel by instance()
    private val adapter = PostsRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onAttachedToWindow = { viewModel.onAttachedToWindow(it) }
        adapter.onItemClickListener = { open(PostDetailsController.newInstance(it)) }
        adapter.onCreateNeedClickListener = { open(CreateNeedController.newInstance()) }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }

        with(view) {
            rvNewsFeed.layoutManager = LinearLayoutManager(context)
            rvNewsFeed.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.newsFeedChannel.openSubscription().bufferize(this@PostsController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.addAll(it.item)
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
    }


    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = PostsController()
    }
}