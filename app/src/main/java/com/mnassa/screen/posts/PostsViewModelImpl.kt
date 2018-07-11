package com.mnassa.screen.posts

import com.mnassa.domain.interactor.PostsInteractor
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
import kotlinx.coroutines.experimental.channels.map
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import kotlin.system.measureTimeMillis

/**
 * Created by Peter on 3/6/2018.
 */
class PostsViewModelImpl(private val postsInteractor: PostsInteractor,
                         private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), PostsViewModel {

    private var isCounterReset = false
    private var resetCounterJob: Job? = null

    override val newsFeedChannel: BroadcastChannel<ListItemEvent<List<PostModel>>> by ProcessAccountChangeArrayBroadcastChannel(
            invokeReConsumeFirstly = true,
            beforeReConsume = {
                isCounterReset = false
                it.send(ListItemEvent.Cleared())
                it.send(ListItemEvent.Added(getNewsFeed()))
            },
            receiveChannelProvider = {
                postsInteractor.loadAll().map { it.toBatched() }
            })

    override val infoFeedChannel: BroadcastChannel<ListItemEvent<InfoPostModel>> by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = { postsInteractor.loadAllInfoPosts() })

    override val permissionsChannel: ConflatedBroadcastChannel<PermissionsModel> by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override fun onAttachedToWindow(post: PostModel) {
        handleException { postsInteractor.onItemViewed(post) }

        //reset counter with debounce
        resetCounterJob?.cancel()
        resetCounterJob = async {
            delay(1_000)
            resetCounter()
        }
    }

    override fun hideInfoPost(post: PostModel) {
        handleException {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
        }
    }

    private fun resetCounter() {
        handleException {
            postsInteractor.resetCounter()
            isCounterReset = true
        }
    }

    private suspend fun getNewsFeed(): List<PostModel> {
        val start = System.currentTimeMillis()
        val result = handleExceptionsSuspend { postsInteractor.loadAllImmediately() } ?: emptyList()
        Timber.e("NewsFeed loading time = ${System.currentTimeMillis() - start}")
        return result
    }
}