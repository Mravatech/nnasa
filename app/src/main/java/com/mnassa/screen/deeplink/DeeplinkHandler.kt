package com.mnassa.screen.deeplink

import android.content.Intent
import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.chats.ChatListController
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.deeplink.NotificationType.AUTO_SUGGEST_YOU_CAN_HELP
import com.mnassa.screen.deeplink.NotificationType.CONNECTIONS_REQUEST_ACCEPTED
import com.mnassa.screen.deeplink.NotificationType.CONNECTION_REQUEST
import com.mnassa.screen.deeplink.NotificationType.EVENT_CANCELLING
import com.mnassa.screen.deeplink.NotificationType.GENERAL_POST_BY_ADMIN
import com.mnassa.screen.deeplink.NotificationType.INVITED_TO_GROUP
import com.mnassa.screen.deeplink.NotificationType.INVITES_NUMBER_CHANGED
import com.mnassa.screen.deeplink.NotificationType.I_WAS_RECOMMENDED
import com.mnassa.screen.deeplink.NotificationType.I_WAS_RECOMMENDED_IN_EVENT
import com.mnassa.screen.deeplink.NotificationType.NEW_EVENT_ATTENDEE
import com.mnassa.screen.deeplink.NotificationType.NEW_EVENT_BY_ADMIN
import com.mnassa.screen.deeplink.NotificationType.NEW_USER_JOINED
import com.mnassa.screen.deeplink.NotificationType.ONE_DAY_TO_EXPIRATION_OF_POST
import com.mnassa.screen.deeplink.NotificationType.POST_COMMENT
import com.mnassa.screen.deeplink.NotificationType.POST_COMMENT_REPLY
import com.mnassa.screen.deeplink.NotificationType.POST_COMMENT_REPLY_2
import com.mnassa.screen.deeplink.NotificationType.POST_IS_EXPIRED
import com.mnassa.screen.deeplink.NotificationType.POST_PROMOTED
import com.mnassa.screen.deeplink.NotificationType.POST_REPOST
import com.mnassa.screen.deeplink.NotificationType.PRIVATE_CHAT_MESSAGE
import com.mnassa.screen.deeplink.NotificationType.RESPONSE_CHAT_MESSAGE
import com.mnassa.screen.deeplink.NotificationType.USER_WAS_RECOMMENDED
import com.mnassa.screen.deeplink.NotificationType.USER_WAS_RECOMMENDED_BY_POST
import com.mnassa.screen.deeplink.NotificationType.USER_WAS_RECOMMENDED_IN_EVENT
import com.mnassa.screen.deeplink.NotificationType.USER_WAS_RECOMMENDED_TO_YOU
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.list.GroupListController
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.profile.ProfileController
import com.mnassa.screen.wallet.WalletController
import timber.log.Timber

/**
 * Created by Peter on 9/5/2018.
 */
interface DeeplinkHandler {
    fun hasDeeplink(intent: Intent?): Boolean
    suspend fun handle(intent: Intent?): Controller?
    suspend fun handle(notificationModel: NotificationModel): Controller?
}

class DeeplinkHandlerImpl(private val postDetailsFactory: PostDetailsFactory,
                          private val inviteSourceHolder: InviteSourceHolder,
                          private val postsInteractor: PostsInteractor,
                          private val eventsInteractor: EventsInteractor,
                          private val profileInteractor: UserProfileInteractor) : DeeplinkHandler {

    override fun hasDeeplink(intent: Intent?): Boolean {
        if (intent == null) return false
        val extras = intent.extras ?: return false
        return !extras.getString(NOTIFICATION_TYPE, null).isNullOrBlank()
    }

    override suspend fun handle(intent: Intent?): Controller? {
        val extras = intent?.extras ?: return null

        return try {
            when {
                extras.containsKey(CHAT_ID) -> {
                    val chatId = extras.getAndRemove(CHAT_ID)?.takeIf { it.isNotBlank() }
                            ?: return ChatListController.newInstance()
                    ChatMessageController.newInstance(chatId)
                }
                extras.containsKey(POST_ID) -> {
                    val postId = extras.getAndRemove(POST_ID)
                    val post = loadPostByIdOrException(postId)
                    postDetailsFactory.newInstance(post)
                }
                extras.containsKey(EVENT_ID) -> {
                    val eventId = extras.getAndRemove(EVENT_ID)
                    val event = loadEventByIdOrException(eventId)
                    EventDetailsController.newInstance(event)
                }
                extras.containsKey(WALLET_AMOUNT) -> {
                    extras.getAndRemove(WALLET_AMOUNT)
                    WalletController.newInstance()
                }
                extras.containsKey(ACCOUNT_ID_1) -> {
                    val accountId = extras.getAndRemove(ACCOUNT_ID_1)?.takeIf { it.isNotBlank() }
                            ?: error("$ACCOUNT_ID_1 is empty")
                    val account = loadProfileByIdOrException(accountId)
                    ProfileController.newInstance(account)
                }
                extras.containsKey(ACCOUNT_ID_2) -> {
                    val accountId = extras.getAndRemove(ACCOUNT_ID_2)?.takeIf { it.isNotBlank() }
                            ?: error("$ACCOUNT_ID_2 is empty")
                    val account = loadProfileByIdOrException(accountId)
                    ProfileController.newInstance(account)
                }
                extras.get(NOTIFICATION_TYPE) == INVITES_NUMBER_CHANGED -> {
                    extras.remove(NOTIFICATION_TYPE)
                    InviteController.newInstance()
                }
                extras.get(NOTIFICATION_TYPE) == INVITED_TO_GROUP -> {
                    extras.remove(NOTIFICATION_TYPE)
                    GroupListController.newInstance()
                }
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun Bundle.getAndRemove(key: String): String? = getString(key).also { remove(key) }

    private fun error(message: String): Nothing {
        throw IllegalArgumentException(message)
    }

    override suspend fun handle(notificationModel: NotificationModel): Controller? {
        return when (notificationModel.type) {
            POST_COMMENT,
            POST_COMMENT_REPLY,
            POST_COMMENT_REPLY_2,
            POST_IS_EXPIRED,
            POST_PROMOTED,
            USER_WAS_RECOMMENDED_BY_POST,
            USER_WAS_RECOMMENDED,
            GENERAL_POST_BY_ADMIN,
            I_WAS_RECOMMENDED,
            AUTO_SUGGEST_YOU_CAN_HELP,
            ONE_DAY_TO_EXPIRATION_OF_POST -> {
                val post = loadPostByIdOrException(notificationModel.extra.post?.id)
                postDetailsFactory.newInstance(post)
            }
            NEW_USER_JOINED,
            POST_REPOST,
            CONNECTION_REQUEST,
            CONNECTIONS_REQUEST_ACCEPTED,
            USER_WAS_RECOMMENDED_TO_YOU,
            PRIVATE_CHAT_MESSAGE,
            RESPONSE_CHAT_MESSAGE -> {
                val account = (
                        notificationModel.extra.recommended
                            ?: notificationModel.extra.reffered
                        )?.id?.let { profileInteractor.getProfileById(it) }
                    ?: notificationModel.extra.author
                    ?: error("Account not found")
                ProfileController.newInstance(account)
            }
            I_WAS_RECOMMENDED_IN_EVENT,
            USER_WAS_RECOMMENDED_IN_EVENT,
            NEW_EVENT_BY_ADMIN,
            NEW_EVENT_ATTENDEE,
            EVENT_CANCELLING -> {
                val event = loadEventByIdOrException(notificationModel.extra.event?.id)
                EventDetailsController.newInstance(event)
            }
            INVITES_NUMBER_CHANGED -> {
                inviteSourceHolder.source = InviteSource.Notification()
                InviteController.newInstance()
            }
            INVITED_TO_GROUP -> {
                val group = notificationModel.extra.group
                if (group != null) GroupDetailsController.newInstance(group)
                else GroupListController.newInstance()
            }
            else -> {
                when {
                    notificationModel.type.toLowerCase().contains(POST_KEYWORD) -> {
                        val post = loadPostByIdOrException(notificationModel.extra.post?.id)
                        postDetailsFactory.newInstance(post)
                    }
                    notificationModel.type.toLowerCase().contains(EVENT_KEYWORD) -> {
                        val event = loadEventByIdOrException(notificationModel.extra.event?.id)
                        EventDetailsController.newInstance(event)
                    }
                    else -> {
                        val account = (
                                notificationModel.extra.recommended
                                    ?: notificationModel.extra.reffered
                                )?.id?.let { profileInteractor.getProfileById(it) }
                            ?: notificationModel.extra.author
                            ?: error("Account not found")
                        ProfileController.newInstance(account)
                    }
                }
            }
        }
    }

    private suspend fun loadPostByIdOrException(postId: String?): PostModel {
        return postsInteractor
            .loadById(postId?.takeIf { it.isNotBlank() } ?: error("Post ID is empty"))
            .receive()
            ?: error("Post[$postId] not found")
    }

    private suspend fun loadEventByIdOrException(eventId: String?): EventModel {
        return eventsInteractor
            .loadByIdChannel(eventId?.takeIf { it.isNotBlank() } ?: error("Event ID is empty"))
            .receive()
            ?: error("Event[$eventId] not found")
    }

    private suspend fun loadProfileByIdOrException(accountId: String?): ShortAccountModel {
        return profileInteractor
            .getProfileById(accountId?.takeIf { it.isNotBlank() } ?: error("Account ID is empty"))
            ?: error("Account[$accountId] not found")
    }

    private companion object {
        const val POST_KEYWORD = "post"
        const val EVENT_KEYWORD = "event"

        const val NOTIFICATION_TYPE = "type"
        const val CHAT_ID = "chatId"
        const val POST_ID = "postId"
        const val EVENT_ID = "eventId"
        const val WALLET_AMOUNT = "amount"
        const val ACCOUNT_ID_1 = "accountId"
        const val ACCOUNT_ID_2 = "id"
    }
}

//TODO: make private or enum
object NotificationType {
    const val PRIVATE_CHAT_MESSAGE = "privateChatMessage"
    const val RESPONSE_CHAT_MESSAGE = "responseChatMessage"
    const val POST_COMMENT = "postComment"
    const val POST_COMMENT_REPLY = "userPostCommentReply"
    const val POST_COMMENT_REPLY_2 = "userCommentReply"
    const val CONNECTION_REQUEST = "connectionRequest"
    const val POST_REPOST = "postRepost"
    const val NEW_USER_JOINED = "newUserJoined"
    const val I_WAS_RECOMMENDED = "iWasRecommended"
    const val I_WAS_RECOMMENDED_IN_EVENT = "iWasRecommendedInEvent"
    const val USER_WAS_RECOMMENDED = "userWasRecommended"
    const val USER_WAS_RECOMMENDED_BY_POST = "userWasRecommendedByPost"
    const val GENERAL_POST_BY_ADMIN = "generalPostByAdmin"
    const val NEW_EVENT_ATTENDEE = "newEventAttendee"
    const val EVENT_CANCELLING = "eventCancelling"
    const val AUTO_SUGGEST_YOU_CAN_HELP = "autoSuggestYouCanHelp"
    const val NEW_EVENT_BY_ADMIN = "newEventByAdmin"
    const val INVITES_NUMBER_CHANGED = "invitesNumberChanged"
    const val CONNECTIONS_REQUEST_ACCEPTED = "connectionsRequestAccepted"
    const val USER_WAS_RECOMMENDED_TO_YOU = "userWasRecommendedToYou"
    const val USER_WAS_RECOMMENDED_IN_EVENT = "userWasRecommendedInEvent"
    const val POST_PROMOTED = "promotePost"
    const val ONE_DAY_TO_EXPIRATION_OF_POST = "oneDayToExpirationOfPost"
    const val POST_IS_EXPIRED = "postIsExpired"
    const val INVITED_TO_GROUP = "youWasInvitedToCommunity"
}