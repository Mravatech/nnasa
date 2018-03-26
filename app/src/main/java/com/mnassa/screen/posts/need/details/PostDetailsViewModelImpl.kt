package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.*
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
    override val commentsChannel: ArrayBroadcastChannel<ListItemEvent<CommentModel>> = ArrayBroadcastChannel(10)
    override val scrollToChannel: ArrayBroadcastChannel<CommentModel> = ArrayBroadcastChannel(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId).consumeEach {
                Timber.d("POST -> send $it")
                postChannel.send(it)
                Timber.d("POST -> sent ok $it")
                postTagsChannel.send(loadTags(it.tags))
                loadComments()
            }
        }
    }

    private fun loadComments() {
        handleException {
            val comments = commentsInteractor.getCommentsByPost(postId)
            Timber.i("COMMENTS: $comments")
            if (comments.isEmpty()) {
                commentsChannel.send(ListItemEvent.Cleared())
            }
            comments.forEach { commentsChannel.send(ListItemEvent.Added(it)) }
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

    override fun createComment(text: String, accountsToRecommend: List<String>, replyTo: CommentModel?) {
        handleException {
            withProgressSuspend {
                val createdComment: CommentModel = when (replyTo) {
                    null -> commentsInteractor.writePostComment(
                            postId = postId,
                            text = text,
                            accountsToRecommend = accountsToRecommend
                    )
                    else -> commentsInteractor.replyToPostComment(
                            postId = postId,
                            text = text,
                            accountsToRecommend = accountsToRecommend,
                            commentId = replyTo.mostParentCommentId
                    )
                }

                commentsChannel.send(ListItemEvent.Added(createdComment))
                scrollToChannel.send(createdComment)
            }
        }
    }
    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { asyncWorker { tagInteractor.get(it) } }.mapNotNull { it.await() }
    }
}