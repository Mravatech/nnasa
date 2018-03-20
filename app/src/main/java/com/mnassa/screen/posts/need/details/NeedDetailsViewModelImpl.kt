package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.model.Post
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/19/2018.
 */
class NeedDetailsViewModelImpl(private val postId: String, private val postsInteractor: PostsInteractor) : MnassaViewModelImpl(), NeedDetailsViewModel {
    override val postChannel: ConflatedChannel<Post> = ConflatedChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId).consumeEach {
                postChannel.send(it)
            }
        }
    }
}