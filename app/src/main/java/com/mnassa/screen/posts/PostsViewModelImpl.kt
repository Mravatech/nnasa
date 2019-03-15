package com.mnassa.screen.posts

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.aggregator.AggregatorLive
import com.mnassa.domain.aggregator.produce
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.PreferencesInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.PostModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.min

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor,
                         private val userProfileInteractor: UserProfileInteractor,
                         private val preferencesInteractor: PreferencesInteractor) : MnassaViewModelImpl(), PostsViewModel {

    private var resetCounterJob: Job? = null

    override val postsLive: AggregatorLive<PostModel>
        get() = postsInteractor.mergedInfoPostsAndFeedLive

    override val permissionsChannel: ConflatedBroadcastChannel<PermissionsModel> by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override val scrollToTopChannel: BroadcastChannel<Unit> = BroadcastChannel(1)

    override val newItemsCounterChannel: BroadcastChannel<Int> = BroadcastChannel(Channel.CONFLATED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            postsLive.produce().consumeEach { state ->
                newItemsCounterChannel.send(state.modelsAllDeltaCount)
            }
        }
    }

    override fun onAttachedToWindow(post: PostModel) {
        GlobalScope.resolveExceptions(showErrorMessage = false) {
            postsInteractor.onItemViewed(post)
        }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = GlobalScope.asyncWorker {
            delay(1_000)
            resetCounter()
        }
    }

    override fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int) {
        val paginationController = postsInteractor.mergedInfoPostsAndFeedPagination
        val paginationSize = min(paginationController.size, totalItemCount.toLong())
        if (visibleItemCount + firstVisibleItemPosition >= paginationSize && firstVisibleItemPosition >= 0) {
            paginationController.requestNextPage(POSTS_PAGE_SIZE)
        }
    }

    override fun setNewItemsTimeUpperBound(date: Date) {
        postsInteractor.mergedInfoPostsAndFeedLiveTimeUpperBound = date
    }

    override fun hideInfoPost(post: PostModel) {
        resolveExceptions {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
        }
    }

    override fun saveScrollPosition(post: PostModel) {
        preferencesInteractor.saveString(KEY_POSTS_POSITION, post.id)
    }

    override fun restoreScrollPosition(): String? {
        return preferencesInteractor.getString(KEY_POSTS_POSITION)
    }

    override fun resetScrollPosition() {
        preferencesInteractor.saveString(KEY_POSTS_POSITION, null)
    }

    private fun resetCounter() {
        resolveExceptions {
            postsInteractor.resetCounter()
        }
    }

    companion object {
        private const val KEY_POSTS_POSITION = "KEY_POSTS_POSITION"

        private const val POSTS_PAGE_SIZE = 60L
    }
}