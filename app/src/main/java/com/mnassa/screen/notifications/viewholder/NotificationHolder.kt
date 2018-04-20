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
        var result: SpannableString = when (type) {
            PRIVATE_CHAT_MESSAGE, RESPONSE_CHAT_MESSAGE -> {
                SpannableString(fromDictionary("$BASE_KEY-"))
            }
            POST_COMMENT -> {
                val commented = fromDictionary("$BASE_KEY$COMMENTED_POST", fromDictionary(R.string.notifications_commented_your_post))
                val postText = fromDictionary(R.string.notifications_i_need) + item.extra.post?.text
                val text = "$commented $postText"
                getOneSpanText(text, postText, Color.BLACK)
            }
            CONNECTION_REQUEST -> {
                SpannableString(fromDictionary("$BASE_KEY$REQUESTED_CONNECT", fromDictionary(R.string.notification_request_to_connect_with_you)))
            }
            POST_REPOST -> {
                SpannableString(fromDictionary("$BASE_KEY$REPOSTED_POST", fromDictionary(R.string.notification_repost_your_post)))
            }
            NEW_USER_JOINED -> {
                SpannableString(fromDictionary("$BASE_KEY$BECAME_MNASSA_USER", fromDictionary(R.string.notification_become_new_mnassa_user)))
            }
            I_WAS_RECOMMENDED -> {
                val recommented = fromDictionary("$BASE_KEY$RECOMMENDED_YOU_IN_POST", fromDictionary(R.string.notification_recommended_you_in_post))
                val postText = fromDictionary(R.string.notifications_i_need) + item.extra.post?.text
                val text = "$recommented $postText"
                getOneSpanText(text, postText, Color.BLACK)
            }
            I_WAS_RECOMMENDED_IN_EVENT -> {
                SpannableString(fromDictionary("$BASE_KEY$RECOMMENDED_YOU_IN_EVENT", fromDictionary(R.string.notification_recommended_you_in_event)))
            }
            USER_WAS_RECOMMENDED -> {
                SpannableString(fromDictionary("$BASE_KEY$RECOMMENDED_YOU", fromDictionary(R.string.notification_recommended_you)))
                SpannableString(fromDictionary("$BASE_KEY$FOR", fromDictionary(R.string.notification_for)))
            }
            USER_WAS_RECOMMENDED_BY_POST -> {
                SpannableString(fromDictionary("$BASE_KEY$ANNOUNCED_YOU", fromDictionary(R.string.notification_announced_your_profile)))
            }
            GENERAL_POST_BY_ADMIN -> {
                SpannableString(fromDictionary("$BASE_KEY$_GENERAL_POST_BY_ADMIN", fromDictionary(R.string.notification_put_your_attention_on_post)))
            }
            NEW_EVENT_ATTENDEE -> {
                SpannableString(fromDictionary("$BASE_KEY$_NEW_EVENT_ATTENDEE", fromDictionary(R.string.notification_is_going_to_attend)))
            }
            AUTO_SUGGEST_YOU_CAN_HELP -> {
                SpannableString(fromDictionary("$BASE_KEY$YOU_CAN_HELP", fromDictionary(R.string.notification_it_seems_you_can_help)))
            }
            NEW_EVENT_BY_ADMIN -> {
                SpannableString(fromDictionary("$BASE_KEY$INVITE_TO_EVENT", fromDictionary(R.string.notification_invited_you_to)))
            }
            INVITES_NUMBER_CHANGED -> {
                SpannableString(fromDictionary("$BASE_KEY$NUMBER_OF_INVITATIONS", fromDictionary(R.string.notification_you_can_send)))
                SpannableString(fromDictionary("$BASE_KEY$NUMBER_OF_INVITATIONS_TAIL", fromDictionary(R.string.notification_more_invites)))
            }
            CONNECTIONS_REQUEST_ACCEPTED -> {
                SpannableString(fromDictionary("$BASE_KEY$CONNECTION_REQUEST_WAS_ACCEPTED", fromDictionary(R.string.notification_requested_to_connect_with_you)))
            }
            USER_WAS_RECOMMENDED_TO_YOU -> {
                val name = getRecommendedName(item)
                val recomend = fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU", fromDictionary(R.string.notification_recommended_you))
                val text = "$recomend $name"
                getOneSpanText(text, name, Color.BLACK)
            }
            USER_WAS_RECOMMENDED_IN_EVENT -> {
                SpannableString(fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU_IN_EVENT", fromDictionary(R.string.notification_thinks)))
                SpannableString(fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU_IN_EVENT_2", fromDictionary(R.string.notification_can_help_you_with)))
            }
            POST_PROMOTED -> {
                SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", fromDictionary(R.string.notification_you_promoted_your_post)))
            }
            ONE_DAY_TO_EXPIRATION_OF_POST -> {
                SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", fromDictionary(R.string.notification_your_post_will_expire_tomorrow)))
            }
            POST_IS_EXPIRED -> {
                SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", fromDictionary(R.string.notification_your_post_has_expired)))
            }
            EVENT_CANCELLING -> {
                val canceled = fromDictionary("$BASE_KEY$EVENT_WAS_CANCELLED", fromDictionary(R.string.notification_was_cancelled_by_organizer))
                val pointsReturns = fromDictionary("${BASE_KEY}$EVENT_WAS_CANCELLED_POINTS_RETURNING", fromDictionary(R.string.notification_points_were_returned))
                val text = "${eventName ?: ""} $canceled ${totalPrice ?: ""} $pointsReturns"
                getEventCancellingText(text, eventName ?: "", totalPrice
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

    private fun getEventCancellingText(text: String, eventName: String, pointsReturns: String, color: Int): SpannableString {
        val span = SpannableString(text)
        span.setSpan(ForegroundColorSpan(color), START_SPAN, eventName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), START_SPAN, eventName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val pointsReturnsPosition = text.indexOf(pointsReturns)
        span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + pointsReturns.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + pointsReturns.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    private fun getOneSpanText(text: String, spanText: String, color: Int): SpannableString {
        val span = SpannableString(text)
        val pointsReturnsPosition = text.indexOf(spanText)
        span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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


const val BASE_KEY = "_notification_text_key"

const val COMMENTED_POST = "_commented_post"
const val REQUESTED_CONNECT = "_requested_connect"
const val REPOSTED_POST = "_reposted_post"
const val BECAME_MNASSA_USER = "_become_mnassa_user"
const val RECOMMENDED_YOU_IN_POST = "_recommended_you_in_post"
const val RECOMMENDED_YOU_IN_EVENT = "_recommended_you_in_event"
const val RECOMMENDED_YOU = "_recommended_you"
const val FOR = "_for"
const val ANNOUNCED_YOU = "_announcedYou"
const val _GENERAL_POST_BY_ADMIN = "_generalPostByAdmin"
const val _NEW_EVENT_ATTENDEE = "_newEventAttendee"
const val YOU_CAN_HELP = "_youCanHelp"
const val INVITE_TO_EVENT = "_inviteToEvent"
const val NUMBER_OF_INVITATIONS = "_numberOfInvitations"
const val NUMBER_OF_INVITATIONS_TAIL = "_numberOfInvitationsTail"
const val CONNECTION_REQUEST_WAS_ACCEPTED = "_connectionRequestWasAccepted"
const val USER_WAS_RECOMENDED_TO_YOU = "_userWasRecommendedToYou"
const val USER_WAS_RECOMENDED_TO_YOU_IN_EVENT = "_userWasRecommendedToYouInEvent"
const val USER_WAS_RECOMENDED_TO_YOU_IN_EVENT_2 = "_userWasRecommendedToYouInEvent2"
const val EVENT_WAS_CANCELLED = "_eventWasCancelled"
const val EVENT_WAS_CANCELLED_POINTS_RETURNING = "_eventWasCancelledPointsReturning"

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