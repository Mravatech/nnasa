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
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.toTimeAgo
import com.mnassa.screen.base.adapter.BasePaginationRVAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.item_notifications.view.*
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
class NotificationHolder(itemView: View, private val onClickListener: View.OnClickListener) : BasePaginationRVAdapter.BaseVH<NotificationModel>(itemView) {

    override fun bind(item: NotificationModel) {

        if (item.extra.author != null) {
            val name = if (item.extra.author?.accountType == AccountType.PERSONAL) {
                "${item.extra.author?.personalInfo?.firstName} ${item.extra.author?.personalInfo?.lastName}"
            } else {
                item.extra.author?.organizationInfo?.organizationName
            }
            itemView.tvUserName.text = name + " | " + item.type
        }
        Timber.i(item.type)
        with(itemView) {
            ivUserIcon.avatarRound(item.extra.author?.avatar)
            tvNotificationInfo.text = getTextByType(item)
            tvNotificationCame.text = item.createdAt.toTimeAgo()
            llNotificationRoot.setOnClickListener(onClickListener)
            llNotificationRoot.tag = this@NotificationHolder
        }
    }

    private fun getTextByType(item: NotificationModel): SpannableString {
        val type = item.type
        val eventName = item.extra.eventName
        val totalPrice = item.extra.totalPrice
        val typeRawValue = type
        val result: SpannableString = when (type) {
            PRIVATE_CHAT_MESSAGE, RESPONSE_CHAT_MESSAGE -> {
                SpannableString(fromDictionary("${fromDictionary(R.string.notification_base_key)}-"))
            }
            POST_COMMENT -> {
                val commented = fromDictionary(fromDictionary(R.string.notification_commented_post), fromDictionary(R.string.notifications_commented_your_post))
                val postText = fromDictionary(R.string.notifications_i_need) + item.extra.post?.text
                val text = "$commented $postText"
                getOneSpanText(text, postText, Color.BLACK)
            }
            CONNECTION_REQUEST -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_request_connect), fromDictionary(R.string.notification_request_to_connect_with_you)))
            }
            POST_REPOST -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_reposted_post), fromDictionary(R.string.notification_repost_your_post)))
            }
            NEW_USER_JOINED -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_become_mnassa_user), fromDictionary(R.string.notification_become_new_mnassa_user)))
            }
            I_WAS_RECOMMENDED -> {
                val recommented = fromDictionary(fromDictionary(R.string.notification_recommend_you_in_post), fromDictionary(R.string.notification_recommended_you_in_post))
                val postText = fromDictionary(R.string.notifications_i_need) + item.extra.post?.text
                val text = "$recommented $postText"
                getOneSpanText(text, postText, Color.BLACK)
            }
            I_WAS_RECOMMENDED_IN_EVENT -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_recommend_you_in_event), fromDictionary(R.string.notification_recommended_you_in_event)))
            }
            USER_WAS_RECOMMENDED -> {
                val name = getRecommendedName(item)
                val recomend = fromDictionary(fromDictionary(R.string.notification_recommend_you), fromDictionary(R.string.notification_recommended_you))
                val _for = fromDictionary(fromDictionary(R.string.notification_for_), fromDictionary(R.string.notification_for))
                val eventName = item.extra.post?.text ?: ""
                val text = "$recomend $name $_for $eventName"
                getTwoSpanText(text, name, eventName, Color.BLACK)
            }
            USER_WAS_RECOMMENDED_BY_POST -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_announced_you), fromDictionary(R.string.notification_announced_your_profile)))
            }
            GENERAL_POST_BY_ADMIN -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_general_post_by_admin), fromDictionary(R.string.notification_put_your_attention_on_post)))
            }
            NEW_EVENT_ATTENDEE -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_new_event_attendee), fromDictionary(R.string.notification_is_going_to_attend)))
            }
            AUTO_SUGGEST_YOU_CAN_HELP -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_you_can_help), fromDictionary(R.string.notification_it_seems_you_can_help)))
            }
            NEW_EVENT_BY_ADMIN -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_invite_to_event), fromDictionary(R.string.notification_invited_you_to)))
            }
            INVITES_NUMBER_CHANGED -> {
                val youCanSend = fromDictionary(fromDictionary(R.string.notification_number_of_invitations), fromDictionary(R.string.notification_you_can_send))
                val moreInvites = fromDictionary(fromDictionary(R.string.notification_number_of_invitations_tail), fromDictionary(R.string.notification_more_invites))
                val text = "$youCanSend ${item.extra.newInviteNumber} $moreInvites"
                getOneSpanText(text, item.extra.newInviteNumber.toString(), Color.BLACK)
            }
            CONNECTIONS_REQUEST_ACCEPTED -> {
                SpannableString(fromDictionary(fromDictionary(R.string.notification_connection_request_was_accepted), fromDictionary(R.string.notification_requested_to_connect_with_you)))
            }
            USER_WAS_RECOMMENDED_TO_YOU -> {
                val name = getRecommendedName(item)
                val recomend = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you), fromDictionary(R.string.notification_recommended_you))
                val text = "$recomend $name"
                getOneSpanText(text, name, Color.BLACK)
            }
            USER_WAS_RECOMMENDED_IN_EVENT -> {
                val thinks = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you_in_event), fromDictionary(R.string.notification_thinks))
                val canHelpYouWith = fromDictionary(fromDictionary(R.string.notification_user_was_recommended_to_you_in_event2), fromDictionary(R.string.notification_can_help_you_with))
                val name = item.extra.event?.author?.userName ?: ""
                val event = item.extra.event?.text ?: ""
                val text = "$thinks $name $canHelpYouWith $event"
                getTwoSpanText(text, name, event, Color.BLACK)
            }
            POST_PROMOTED -> {
                SpannableString(fromDictionary("${fromDictionary(R.string.notification_base_key)}_$typeRawValue", fromDictionary(R.string.notification_you_promoted_your_post)))
            }
            ONE_DAY_TO_EXPIRATION_OF_POST -> {
                SpannableString(fromDictionary("${fromDictionary(R.string.notification_base_key)}_$typeRawValue", fromDictionary(R.string.notification_your_post_will_expire_tomorrow)))
            }
            POST_IS_EXPIRED -> {
                SpannableString(fromDictionary("${fromDictionary(R.string.notification_base_key)}_$typeRawValue", fromDictionary(R.string.notification_your_post_has_expired)))
            }
            EVENT_CANCELLING -> {
                val canceled = fromDictionary(fromDictionary(R.string.notification_event_was_cancelled), fromDictionary(R.string.notification_was_cancelled_by_organizer))
                val pointsReturns = fromDictionary(fromDictionary(R.string.notification_event_was_cancelled_points_returning), fromDictionary(R.string.notification_points_were_returned))
                val text = "${eventName ?: ""} $canceled ${totalPrice ?: ""} $pointsReturns"
                getTwoSpanText(text, eventName ?: "", totalPrice
                        ?: "", Color.BLACK)

            }
            else -> SpannableString(item.text)
        }
        return result
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

        const val START_SPAN = 0
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NotificationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationHolder(view, onClickListener)
        }
    }
}

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
const val POST_PROMOTED = "postPromoted"
const val ONE_DAY_TO_EXPIRATION_OF_POST = "oneDayToExpirationOfPost"
const val POST_IS_EXPIRED = "postIsExpired"