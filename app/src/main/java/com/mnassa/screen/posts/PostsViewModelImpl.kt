package com.mnassa.screen.posts

import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

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
                Timber.e("preloadFeed >>> beforeReConsume - clear!")
                it.send(ListItemEvent.Cleared())
                it.send(ListItemEvent.Added(postsInteractor.getPreloadedFeed()))
                Timber.e("preloadFeed >>> beforeReConsume - sent")
            },
            receiveChannelProvider = {
                postsInteractor.loadFeedWithChangesHandling().map { it.toBatched() }//.bufferize(this)
            })
    override val newsFeedUpdatesChannel: BroadcastChannel<ListItemEvent<List<PostModel>>> = ArrayBroadcastChannel(10)
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

    override fun onDetachedFromWindow(post: PostModel) {

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
            try {
                postsInteractor.resetCounter()
                isCounterReset = true
            } catch (e: NetworkException) {
                //ignore
            }
        }
    }
}