package com.mnassa.screen.comments

import android.net.Uri
import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RawCommentModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by Peter on 4/18/2018.
 */
class CommentsWrapperForEventViewModelImpl(
        private val eventId: String,
        private val commentsInteractor: CommentsInteractor,
        private val eventsInteractor: EventsInteractor
) : MnassaViewModelImpl(), CommentsWrapperViewModel {
    override val scrollToChannel: BroadcastChannel<CommentModel> = BroadcastChannel(1)
    override val commentsChannel: ConflatedBroadcastChannel<List<CommentModel>> = ConflatedBroadcastChannel()
    override val canReadCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    override val canWriteCommentsChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel(true)
    private val preloadedImages = HashMap<Uri, Deferred<String?>>()
    private val commentMutex = Mutex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            loadComments() //load comments even if event is not available
            eventsInteractor.loadByIdChannel(eventId).consumeEach { event ->
                if (event != null) {
                    loadComments()
                }
            }
        }
    }

    override suspend fun createComment(comment: RawCommentModel): Boolean {
        return commentMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    comment.uploadImages()
                    val createdComment: CommentModel = when (comment.parentCommentId) {
                        null -> commentsInteractor.writeEventComment(comment)
                        else -> commentsInteractor.replyToEventComment(comment)
                    }

                    commentsChannel.send((commentsChannel.valueOrNull
                            ?: emptyList()) + createdComment)
                    scrollToChannel.send(createdComment)
                }
                true
            } ?: false
        }
    }

    override suspend fun editComment(comment: RawCommentModel): Boolean {
        return commentMutex.withLock {
            handleExceptionsSuspend {
                withProgressSuspend {
                    comment.uploadImages()
                    commentsInteractor.editEventComment(comment)
                    loadComments()
                }
                true
            } ?: false
        }
    }

    override fun deleteComment(commentModel: CommentModel) {
        resolveExceptions {
            commentMutex.withLock {
                withProgressSuspend {
                    commentsInteractor.deleteEventComment(commentModel)
                    //TODO: remove this when comments counter will be fixed on the server side
                    loadComments()
                }
            }
        }
    }

    override fun preloadImage(imageFile: Uri) {
        resolveExceptions {
            uploadImageIfNeeded(imageFile)
        }
    }

    private suspend fun uploadImageIfNeeded(imageFile: Uri) = preloadedImages.getOrPut(imageFile) {
        asyncWorker {
            handleExceptionsSuspend { commentsInteractor.preloadCommentImage(imageFile) }
        }
    }

    private suspend fun loadComments() {
        handleExceptionsSuspend {
            try {
                commentsChannel.send(commentsInteractor.getCommentsByEvent(eventId))
            } catch (e: NoRightsToComment) {
                canReadCommentsChannel.send(e.canReadComments)
                canWriteCommentsChannel.send(e.canWriteComments)
            }
        }
    }

    private suspend fun RawCommentModel.uploadImages() {
        val uploadedImagesResult = ArrayList(uploadedImages)
        uploadedImagesResult += imagesToUpload.map { uploadImageIfNeeded(it) }.mapNotNull { it.await() }
        uploadedImages = uploadedImagesResult
        imagesToUpload = emptyList()
    }
}