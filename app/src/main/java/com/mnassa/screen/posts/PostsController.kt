package com.mnassa.screen.posts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.aggregator.produce
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.extensions.canBeShared
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.subscribeToUpdates
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.main.PageContainer
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.NeedDetailsController
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.posts.profile.create.RecommendUserController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.newpanel.NewPanelView
import kotlinx.android.synthetic.main.controller_posts_list.view.*
import kotlinx.android.synthetic.main.sub_profile_header.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consume
import org.kodein.di.generic.instance


/**
 * Created by Peter on 3/6/2018.
 */
class PostsController : MnassaControllerImpl<PostsViewModel>(),
        NewPanelView,
        OnPageSelected,
        OnScrollToTop,
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_posts_list
    override val viewModel: PostsViewModel by instance()
    protected var post: PostModel? = null

    private val adapter = PostsRVAdapter(this)
    private val controllerSelectedExecutor = StateExecutor<Unit, Unit>(initState = Unit) {
        val parent = parentController
        parent is PageContainer && parent.isPageSelected(this@PostsController)
    }

    override var sharingOptions: PostPrivacyOptions = PostPrivacyOptions.PUBLIC
        set(value) {
            field = value
            GlobalScope.launchUI {
                getViewSuspend()
                viewModel.repost(value)
            }
        }

    private var postIdToScroll: String? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            adapter.restoreState(this)
        }



        adapter.onRepostClickListener = {
            Log.e("REPOST_CLICKED", adapter.toString())
//            Log.e("REPOST_CLICKED", it.counters.reposts.toString())


            val sharingOptionsController = SharingOptionsController.newInstance(
                    listener = this,
                    accountsToExclude = listOf(it.author.id),
                    restrictShareReduction = false,
                    canBePromoted = false,
                    promotePrice = 0L)

//            if (this is SharingOptionsController.OnSharingOptionsResult){
//                Log.e("is it", "Yes, it is!")
//            }else{
//                Log.e("is it", "No, it is not!")
//
//            }

            open(SharingOptionsController.newInstance(
                    listener = this,
                    accountsToExclude = listOf(it.author.id),
                    restrictShareReduction = false,
                    canBePromoted = false,
                    promotePrice = 0L))

        }
        adapter.onOffersClickListener = { post, account ->

            open(ChatMessageController.newInstance(post, account))
        }

        adapter.onRecommendationClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }

        adapter.onAttachedToWindow = { post ->
            controllerSelectedExecutor.invoke { viewModel.onAttachedToWindow(post) }
        }
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
        adapter.onHideInfoPostClickListener = viewModel::hideInfoPost
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled

            if (postIdToScroll != null) {
                val dataIndex = adapter.dataStorage.indexOfFirst { it.id == postIdToScroll }
                if (dataIndex >= 0) {
                    postIdToScroll = null
                    val layoutManager = view?.rvNewsFeed?.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        layoutManager.scrollToPosition(adapter.convertDataIndexToAdapterPosition(dataIndex))
                    }
                }
            }
        }

        resolveExceptions {
            launchCoroutineUI {
                viewModel.postsLive.produce().subscribeToUpdates(
                        adapter = adapter,
                        emptyView = { getViewSuspend().rlEmptyView },
                        onAdded = { triggerScrollPanel() }
                )
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.rvNewsFeed.itemAnimator = null
        view.rvNewsFeed.adapter = adapter

        launchUI {
            view.rvNewsFeed.setupNewPanel(viewModel)
        }

        view.rvNewsFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                viewModel.onScroll(visibleItemCount, totalItemCount, firstVisibleItemPosition)
            }
        })
    }

    override fun formatNewPanelLabel(counter: Int): String? {
        val text = fromDictionary(resources!!.getString(R.string.posts_new_items_available))
        return try {
            text.format(counter)
        } catch (e: Exception) {
            null
        }
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