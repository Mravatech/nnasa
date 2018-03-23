package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 3/19/2018.
 */
class PostDetailsViewModelImpl(private val postId: String,
                               private val postsInteractor: PostsInteractor,
                               private val tagInteractor: TagInteractor,
                               private val commentsInteractor: CommentsInteractor) : MnassaViewModelImpl(), PostDetailsViewModel {
    override val postChannel: ConflatedBroadcastChannel<Post> = ConflatedBroadcastChannel()
    override val postTagsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val finishScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId).consumeEach {
                Timber.d("POST -> send $it")
                postChannel.send(it)
                Timber.d("POST -> sent ok $it")
                postTagsChannel.send(loadTags(it.tags))
            }
        }

        handleException {
            val comments = commentsInteractor.getCommentsByPost(postId)
            Timber.i("COMMENTS: $comments")
        }
    }

    override fun delete() {
        handleException {
            withProgressSuspend {
                postsInteractor.removePost(postId)
                finishScreenChannel.send(Unit)
            }
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { asyncWorker { tagInteractor.get(it) } }.mapNotNull { it.await() }
    }
}