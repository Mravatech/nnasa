package com.mnassa.screen.comments

import android.net.Uri
import android.os.Bundle
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RawCommentModel
import com.mnassa.domain.model.RewardModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
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
    private val preloadedImages = HashMap<Uri, Deferred<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            postsInteractor.loadById(postId, postAuthorId).consumeEach { post ->
                if (post != null) {
                    loadComments()
                } else {
                    canReadCommentsChannel.send(false)
                    canWriteCommentsChannel.send(false)
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

    override suspend fun createComment(comment: RawCommentModel): Boolean {
        return handleExceptionsSuspend {
            withProgressSuspend {
                comment.uploadImages()
                val createdComment: CommentModel = when (comment.parentCommentId) {
                    null -> commentsInteractor.writePostComment(comment)
                    else -> commentsInteractor.replyToPostComment(comment)
                }
                commentsChannel.send((commentsChannel.valueOrNull ?: emptyList()) + createdComment)
                scrollToChannel.send(createdComment)
            }
            true
        } ?: false
    }

    override suspend fun editComment(comment: RawCommentModel): Boolean {
        return handleExceptionsSuspend {
            withProgressSuspend {
                comment.uploadImages()
                commentsInteractor.editPostComment(comment)
                loadComments()
            }
            true
        } ?: false
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

    override fun preloadImage(imageFile: Uri) {
        handleException {
            uploadImageIfNeeded(imageFile)
        }
    }

    private suspend fun uploadImageIfNeeded(imageFile: Uri) = preloadedImages.getOrPut(imageFile) { async { commentsInteractor.preloadCommentImage(imageFile) } }

    private suspend fun loadComments() {
        try {
            commentsChannel.send(commentsInteractor.getCommentsByPost(postId))
        } catch (e: NoRightsToComment) {
            canReadCommentsChannel.send(e.canReadComments)
            canWriteCommentsChannel.send(e.canWriteComments)
        }
    }

    private suspend fun RawCommentModel.uploadImages() {
        val uploadedImagesResult = ArrayList(uploadedImages)
        uploadedImagesResult += imagesToUpload.map { uploadImageIfNeeded(it) }.map { it.await() }
        uploadedImages = uploadedImagesResult
        imagesToUpload = emptyList()
    }
}