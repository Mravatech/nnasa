package com.mnassa.screen.notifications.viewholder

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mnassa.R
import com.mnassa.domain.model.AccountType
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.screen.notifications.NotificationAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_notifications.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<NotificationAdapter.NotificationItem>(itemView) {

    override fun bind(item: NotificationAdapter.NotificationItem) {
        val item = (item as NotificationAdapter.NotificationItem.ContentItem).content

        if (item.extra.author != null) {
            itemView.tvUserName.text = item.extra.author?.formattedName
        }
        with(itemView) {
            itemView.ivUserIcon.avatarRound(item.extra.author?.avatar)
            tvNotificationCame.text = item.createdAt.toTimeAgo()
            llNotificationRoot.setOnClickListener(onClickListener)
            llNotificationRoot.tag = this@NotificationHolder
        }
        setViewsByType(item)
    }

    private fun setViewsByType(item: NotificationModel) {
        when (item.type) {
            PRIVATE_CHAT_MESSAGE, RESPONSE_CHAT_MESSAGE -> {
                itemView.tvNotificationInfo.text = fromDictionary("${fromDictionary(R.string.notification_base_key)}-")
            }
            I_WAS_RECOMMENDED -> {
                val recommended = fromDictionary(fromDictionary(R.string.notification_recommend_you_in_post), fromDictionary(R.string.notification_recommended_you_in_post))
                val postText = "${fromDictionary(R.string.notifications_i_need)} ${item.extra.post?.text}"
                val text = "$recommended $postText"
                itemView.tvNotificationInfo.text = getOneSpanText(text, postText, Color.BLACK)
            }
            I_WAS_RECOMMENDED_IN_EVENT -> {
                val recommended = fromDictionary(fromDictionary(R.string.notification_recommend_you_in_event), fromDictionary(R.string.notification_recommended_you_in_event))
                val eventText = item.extra.event?.text ?: ""
                val text = "$recommended $eventText"
                itemView.tvNotificationInfo.text = getOneSpanText(text, eventText, Color.BLACK)
            }
            USER_WAS_RECOMMENDED -> {
                val name = getRecommendedName(item)
                val recommend = fromDictionary(fromDictionary(R.string.notification_recommend_you), fromDictionary(R.string.notification_recommended_you))
                val _for = fromDictionary(fromDictionary(R.string.notification_for_), fromDictionary(R.string.notification_for))
                val eventName = item.extra.post?.text ?: ""
                val text = "$recommend $name $_for $eventName"
                itemView.tvNotificationInfo.text = getTwoSpanText(text, name, eventName, Color.BLACK)
            }
            CONNECTION_REQUEST -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_request_connect), fromDictionary(R.string.notification_request_to_connect_with_you))
            }
            POST_COMMENT -> {
                val commented = fromDictionary(fromDictionary(R.string.notification_commented_post), fromDictionary(R.string.notifications_commented_your_post))
                val postText = "${fromDictionary(R.string.notifications_i_need)} ${item.extra.post?.text}"
                val text = "$commented $postText"
                itemView.tvNotificationInfo.text = getOneSpanText(text, postText, Color.BLACK)
            }
            NEW_EVENT_BY_ADMIN -> {
                val invited = fromDictionary(fromDictionary(R.string.notification_invite_to_event), fromDictionary(R.string.notification_invited_you_to))
                val eventText = item.extra.event?.text ?: ""
                val text = "$invited $eventText"
                itemView.tvNotificationInfo.text = getOneSpanText(text, eventText, Color.BLACK)
            }
            POST_REPOST -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_reposted_post), fromDictionary(R.string.notification_repost_your_post))
            }
            NEW_USER_JOINED -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_become_mnassa_user), fromDictionary(R.string.notification_become_new_mnassa_user))
            }
            USER_WAS_RECOMMENDED_BY_POST -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_announced_you), fromDictionary(R.string.notification_announced_your_profile))
            }
            AUTO_SUGGEST_YOU_CAN_HELP -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_you_can_help), fromDictionary(R.string.notification_it_seems_you_can_help))
            }
            USER_WAS_RECOMMENDED_TO_YOU -> {
                val authorName = item.extra.author?.formattedName ?: ""
                val name = item.extra.recommended?.formattedName ?: ""
                val recommend = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you), fromDictionary(R.string.notification_recommended_you))
                val text = "$authorName $recommend $name"
                itemView.tvNotificationInfo.text = getTwoSpanText(text, authorName, name, Color.BLACK)
            }
            ONE_DAY_TO_EXPIRATION_OF_POST -> {
                itemView.ivUserIcon.avatarRound(item.extra.post?.author?.avatar)
                itemView.tvNotificationInfo.text = fromDictionary("${fromDictionary(R.string.notification_base_key)}_${item.type}", fromDictionary(R.string.notification_your_post_will_expire_tomorrow))
            }
            POST_IS_EXPIRED -> {
                itemView.tvNotificationInfo.text = fromDictionary("${fromDictionary(R.string.notification_base_key)}_${item.type}", fromDictionary(R.string.notification_your_post_has_expired))
            }
            NEW_EVENT_ATTENDEE -> {
                val isGoingToAttend = fromDictionary(fromDictionary(R.string.notification_new_event_attendee), fromDictionary(R.string.notification_is_going_to_attend))
                val eventName = item.extra.eventName ?: ""
                val text = "${item.extra.attendee} $isGoingToAttend $eventName"
                itemView.tvNotificationInfo.text = getOneSpanText(text, eventName, Color.BLACK)
            }
            USER_WAS_RECOMMENDED_IN_EVENT -> {
                val thinks = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you_in_event), fromDictionary(R.string.notification_thinks))
                val canHelpYouWith = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you_in_event2), fromDictionary(R.string.notification_can_help_you_with))
                val name = item.extra.event?.author?.formattedName ?: ""
                val event = item.extra.event?.text ?: ""
                val text = "$thinks $name $canHelpYouWith $event"
                itemView.tvNotificationInfo.text = getTwoSpanText(text, name, event, Color.BLACK)
            }
            INVITES_NUMBER_CHANGED -> {
                val youCanSend = fromDictionary(fromDictionary(R.string.notification_number_of_invitations), fromDictionary(R.string.notification_you_can_send))
                val moreInvites = fromDictionary(fromDictionary(R.string.notification_number_of_invitations_tail), fromDictionary(R.string.notification_more_invites))
                val text = "$youCanSend ${item.extra.newInviteNumber} $moreInvites"
                itemView.tvNotificationInfo.text = getOneSpanText(text, item.extra.newInviteNumber.toString(), Color.BLACK)
            }
            CONNECTIONS_REQUEST_ACCEPTED -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_connection_request_was_accepted), fromDictionary(R.string.notification_requested_to_connect_with_you))
            }
            GENERAL_POST_BY_ADMIN -> {
                itemView.tvNotificationInfo.text = fromDictionary(fromDictionary(R.string.notification_general_post_by_admin), fromDictionary(R.string.notification_put_your_attention_on_post))
            }
            POST_PROMOTED -> {
                itemView.tvNotificationInfo.text = fromDictionary("${fromDictionary(R.string.notification_base_key)}_${item.type}", fromDictionary(R.string.notification_you_promoted_your_post))
            }
            EVENT_CANCELLING -> {
                val canceled = fromDictionary(fromDictionary(R.string.notification_event_was_cancelled), fromDictionary(R.string.notification_was_cancelled_by_organizer))
                val pointsReturns = fromDictionary(fromDictionary(R.string.notification_event_was_cancelled_points_returning), fromDictionary(R.string.notification_points_were_returned))
                val eventName = item.extra.eventName ?: ""
                val totalPrice = item.extra.totalPrice ?: ""
                val text = "$eventName $canceled $totalPrice $pointsReturns"
                itemView.tvNotificationInfo.text = getTwoSpanText(text, eventName, totalPrice, Color.BLACK)
            }
//            INVITED_TO_GROUP -> {
//
//            }
            else -> itemView.tvNotificationInfo.text = item.text
        }
    }

    private fun getRecommendedName(item: NotificationModel): String {
        val recommended = item.extra.recommended ?: return ""
        val name = if (recommended.accountType == AccountType.PERSONAL) {
            "${recommended.personalInfo?.firstName} ${recommended.personalInfo?.lastName}"
        } else {
            recommended.organizationInfo?.organizationName
        }
        return requireNotNull(name)
    }

    private fun getOneSpanText(text: String, spanText: String, color: Int): SpannableString {
        val span = SpannableString(text)
        val pointsReturnsPosition = text.indexOf(spanText)
        span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    private fun getTwoSpanText(text: String, spanText1: String, spanText2: String, color: Int): SpannableString {
        val span = SpannableString(text)
        val pointsReturnsPosition = text.indexOf(spanText1)
        val pointsSecondReturnsPosition = text.indexOf(spanText2)
        span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + spanText1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(ForegroundColorSpan(color), pointsSecondReturnsPosition, pointsSecondReturnsPosition + spanText2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + spanText1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsSecondReturnsPosition, pointsSecondReturnsPosition + spanText2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    companion object {
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NotificationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationHolder(view, onClickListener)
        }
    }
}
//TODO: remove this
const val PRIVATE_CHAT_MESSAGE = "privateChatMessage"
const val RESPONSE_CHAT_MESSAGE = "responseChatMessage"
const val POST_COMMENT = "postComment"
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