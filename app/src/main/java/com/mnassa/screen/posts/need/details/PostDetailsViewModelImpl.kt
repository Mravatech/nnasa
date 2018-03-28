package com.mnassa.screen.posts.need.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
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
                               private val commentsInteractor: CommentsInteractor)
    : MnassaViewModelImpl(), PostDetailsViewModel {
    override val postChannel: ConflatedBroadcastChannel<PostModel> = ConflatedBroadcastChannel()
    override val postTagsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val finishScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    override val commentsChannel: ConflatedBroadcastChannel<List<CommentModel>> = ConflatedBroadcastChannel()
    override val scrollToChannel: ArrayBroadcastChannel<CommentModel> = ArrayBroadcastChannel(1)
    override val canReadCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    override val canWriteCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)

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
            try {
                commentsChannel.send(commentsInteractor.getCommentsByPost(postId))
            } catch (e: NoRightsToComment) {
                canReadCommentsChannel.send(e.canReadComments)
                canWriteCommentsChannel.send(e.canWriteComments)
            }
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

                commentsChannel.send((commentsChannel.valueOrNull ?: emptyList()) + createdComment)
                scrollToChannel.send(createdComment)
            }
        }
    }

    override fun deleteComment(commentModel: CommentModel) {
        handleException {
            withProgressSuspend {
                commentsInteractor.deleteComment(commentModel.id)
                //TODO: remove this when comments counter will be fixed on the server side
                loadComments()
            }
        }
    }

    override fun repost(sharingOptions: SharingOptionsController.ShareToOptions) {
        handleException {
            withProgressSuspend {
                postsInteractor.repostPost(postId, null, sharingOptions.selectedConnections)
            }
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { asyncWorker { tagInteractor.get(it) } }.mapNotNull { it.await() }
    }
}