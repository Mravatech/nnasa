package com.mnassa.screen.comments

import android.net.Uri
import android.os.Bundle
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RawCommentModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/18/2018.
 */
class CommentsWrapperForEventViewModelImpl(
        private val eventId: String,
        private val commentsInteractor: CommentsInteractor,
        private val eventsInteractor: EventsInteractor
) : MnassaViewModelImpl(), CommentsWrapperViewModel {
    override val scrollToChannel: ArrayBroadcastChannel<CommentModel> = ArrayBroadcastChannel(1)
    override val commentsChannel: ConflatedBroadcastChannel<List<CommentModel>> = ConflatedBroadcastChannel()
    override val canReadCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    override val canWriteCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    private val preloadedImages = HashMap<Uri, Deferred<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            loadComments() //load comments even if event is not available
            eventsInteractor.loadByIdChannel(eventId).consumeEach { event ->
                if (event != null) {
                    loadComments()
                }
            }
        }
    }

    override suspend fun createComment(comment: RawCommentModel): Boolean {
        return handleExceptionsSuspend {
            withProgressSuspend {
                comment.uploadImages()
                val createdComment: CommentModel = when (comment.parentCommentId) {
                    null -> commentsInteractor.writeEventComment(comment)
                    else -> commentsInteractor.replyToEventComment(comment)
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
                commentsInteractor.editEventComment(comment)
                loadComments()
            }
            true
        } ?: false
    }

    override fun deleteComment(commentModel: CommentModel) {
        handleException {
            withProgressSuspend {
                commentsInteractor.deleteEventComment(commentModel)
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
            commentsChannel.send(commentsInteractor.getCommentsByEvent(eventId))
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