package com.mnassa.screen.posts.info.details

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.PostModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 4/30/2018.
 */
class InfoDetailsViewModelImpl(
        private val postsInteractor: PostsInteractor) : MnassaViewModelImpl(), InfoDetailsViewModel {
    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)

    override fun hidePost(post: PostModel) {
        resolveExceptions {
            withProgressSuspend {
                postsInteractor.hideInfoPost(post.id)
            }
            closeScreenChannel.send(Unit)
        }
    }
}