package com.mnassa.screen.posts

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaControllerImpl
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
        with(view) {
            rvNewsFeed.layoutManager = LinearLayoutManager(context)
            rvNewsFeed.adapter = adapter
        }

        adapter.isLoadingEnabled = true

        launchCoroutineUI {

            viewModel.getNewsFeedChannel().consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.add(it.item)
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.remove(it.item)
                }
            }
        }
    }


    companion object {
        fun newInstance() = PostsController()
    }
}