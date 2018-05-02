package com.mnassa.screen.comments

import android.os.Bundle
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.interactor.CommentsInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RewardModel
import com.mnassa.domain.model.mostParentCommentId
import com.mnassa.screen.base.MnassaViewModelImpl
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsInteractor.loadByIdChannel(eventId).consumeEach { event ->
                if (event != null) {
                    loadComments()
                }
            }
        }
    }

    override fun sendPointsForComment(rewardModel: RewardModel) {}

    override fun createComment(text: String, accountsToRecommend: List<String>, replyTo: CommentModel?) {
        handleException {
            withProgressSuspend {
                val createdComment: CommentModel = when (replyTo) {
                    null -> commentsInteractor.writeEventComment(
                            eventId = eventId,
                            text = text,
                            accountsToRecommend = accountsToRecommend
                    )
                    else -> commentsInteractor.replyToEventComment(
                            eventId = eventId,
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
                commentsInteractor.editPostComment(originalComment.id, text, accountsToRecommend)
                loadComments()
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

    private suspend fun loadComments() {
        try {
            commentsChannel.send(commentsInteractor.getCommentsByEvent(eventId))
        } catch (e: NoRightsToComment) {
            canReadCommentsChannel.send(e.canReadComments)
            canWriteCommentsChannel.send(e.canWriteComments)
        }
    }
}