package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/19/2018.
 */
class NeedDetailsViewModelImpl(private val postId: String,
                               private val postsInteractor: PostsInteractor,
                               private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), NeedDetailsViewModel {
    override val postChannel: ConflatedChannel<Post> = ConflatedChannel()
    override val postTagsChannel: ConflatedChannel<List<TagModel>> = ConflatedChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId).consumeEach {
                postChannel.send(it)
                postTagsChannel.send(loadTags(it.tags))
            }
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { asyncWorker { tagInteractor.get(it) } }.mapNotNull { it.await() }
    }
}