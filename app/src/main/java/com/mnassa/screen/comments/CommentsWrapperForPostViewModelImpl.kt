package com.mnassa.screen.comments

import android.os.Bundle
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.CommentReplyModel
import com.mnassa.domain.model.RewardModel
import com.mnassa.domain.model.mostParentCommentId
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/17/2018.
 */
class CommentsWrapperForPostViewModelImpl(
        private val postId: String,
        private val postAuthorId: String,
        private val commentsInteractor: CommentsInteractor,
        private val postsInteractor: PostsInteractor,
        private val walletInteractor: WalletInteractor
) : MnassaViewModelImpl(), CommentsWrapperViewModel {

    override val scrollToChannel: ArrayBroadcastChannel<CommentModel> = ArrayBroadcastChannel(1)
    override val commentsChannel: ConflatedBroadcastChannel<List<CommentModel>> = ConflatedBroadcastChannel()
    override val canReadCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    override val canWriteCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId, postAuthorId).consumeEach { post ->
                if (post != null) {
                    loadComments()
                }
            }
        }
    }

    override fun sendPointsForComment(rewardModel: RewardModel) {
        handleException {
            withProgressSuspend {
                walletInteractor.sendPointsForComment(rewardModel)
                loadComments()
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

    override fun editComment(originalComment: CommentModel, text: String, accountsToRecommend: List<String>, replyTo: CommentModel?) {
        handleException {
            withProgressSuspend {
                commentsInteractor.editPostComment(
                        originalCommentId = originalComment.id,
                        text = text,
                        accountsToRecommend = accountsToRecommend,
                        parentCommentId = (originalComment as? CommentReplyModel)?.parentId
                )
                loadComments()
            }
        }
    }

    override fun deleteComment(commentModel: CommentModel) {
        handleException {
            withProgressSuspend {
                commentsInteractor.deletePostComment(commentModel)
                //TODO: remove this when comments counter will be fixed on the server side
                loadComments()
            }
        }
    }

    private suspend fun loadComments() {
        try {
            commentsChannel.send(commentsInteractor.getCommentsByPost(postId))
        } catch (e: NoRightsToComment) {
            canReadCommentsChannel.send(e.canReadComments)
            canWriteCommentsChannel.send(e.canWriteComments)
        }
    }
}