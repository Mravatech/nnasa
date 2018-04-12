package com.mnassa.screen.posts.profile.details

import android.os.Bundle
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.RecommendedProfilePostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.posts.need.details.NeedDetailsViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/10/2018.
 */
class RecommendedProfileViewModelImpl(postId: String,
                                      postsInteractor: PostsInteractor,
                                      tagInteractor: TagInteractor,
                                      commentsInteractor: CommentsInteractor,
                                      private val connectionsInteractor: ConnectionsInteractor) : NeedDetailsViewModelImpl(
        postId,
        postsInteractor,
        tagInteractor,
        commentsInteractor
), RecommendedProfileViewModel {

    override val connectionStatusChannel: ConflatedBroadcastChannel<ConnectionStatus> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postChannel.consumeEach { post ->
                loadConnectionStatus(post as RecommendedProfilePostModel)
            }
        }
    }

    private var loadConnectionStatusJob: Job? = null
    private fun loadConnectionStatus(recommendedProfilePostModel: RecommendedProfilePostModel) {
        loadConnectionStatusJob?.cancel()
        loadConnectionStatusJob = handleException {
            connectionsInteractor.getStatusesConnections(recommendedProfilePostModel.recommendedProfile.id).consumeEach {
                connectionStatusChannel.send(it)
            }
        }
    }

    override fun connect(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                connectionsInteractor.actionConnect(listOf(account.id))
            }
        }
    }
}