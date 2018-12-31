package com.mnassa.screen.posts

import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.PreferencesInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.InfoPostModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay
import java.util.*

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor,
                         private val userProfileInteractor: UserProfileInteractor,
                         private val preferencesInteractor: PreferencesInteractor) : MnassaViewModelImpl(), PostsViewModel {

    private var isCounterReset = false
    private var resetCounterJob: Job? = null

    override val newsFeedChannel: BroadcastChannel<ListItemEvent<List<PostModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            beforeReConsume = {
                isCounterReset = false
                it.send(ListItemEvent.Cleared())
            },
            receiveChannelProvider = {
                postsInteractor.loadMergedInfoPostsAndFeed()
            })

    override val permissionsChannel: ConflatedBroadcastChannel<PermissionsModel> by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override val recheckFeedsChannel: BroadcastChannel<ListItemEvent<List<PostModel>>> = BroadcastChannel(10)

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = async {
            delay(1_000)
            resetCounter()
        }
    }

    override fun recheckFeeds() {
        handleException {
            val removedPosts = postsInteractor.recheckAndReloadFeeds()
            recheckFeedsChannel.send(removedPosts)
        }
    }

    override fun hideInfoPost(post: PostModel) {
        handleException {
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

    override fun getLastViewedPostDate(): Date? {
        return preferencesInteractor.getLong(KEY_POSTS_LAST_VIEWED, -1).takeIf { it >= 0 }?.let { Date(it) }
    }

    override fun setLastViewedPostDate(date: Date?) {
        preferencesInteractor.saveLong(KEY_POSTS_LAST_VIEWED, date?.time ?: -1)
    }

    private fun resetCounter() {
        handleException {
            try {
                postsInteractor.resetCounter()
                isCounterReset = true
            } catch (e: NetworkException) {
                //ignore
            }
        }
    }

    companion object {
        private const val KEY_POSTS_POSITION = "KEY_POSTS_POSITION"
        private const val KEY_POSTS_LAST_VIEWED = "KEY_POSTS_LAST_VIEWED"
    }
}