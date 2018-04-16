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
import com.mnassa.screen.notifications.*
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
        var result: SpannableString
        when (type) {
            privateChatMessage, responseChatMessage -> {
                result = SpannableString(fromDictionary("$BASE_KEY-"))
            }
            postComment -> {
                val commented = fromDictionary("$BASE_KEY$COMMENTED_POST", COMMENTED_POST_DEFAULT_VALUE)
                val postText = COMMENTED_POST_I_NEED + item.extra.post?.text
                val text = "$commented $postText"
                result = getOneSpanText(text, postText, Color.BLACK)
            }
            connectionRequest -> {
                result = SpannableString(fromDictionary("$BASE_KEY$REQUESTED_CONNECT", REQUESTED_CONNECT_DEFAULT_VALUE))
            }
            postRepost -> {
                result = SpannableString(fromDictionary("$BASE_KEY$REPOSTED_POST", REPOSTED_POST_DEFAULT_VALUE))
            }
            newUserJoined -> {
                result = SpannableString(fromDictionary("$BASE_KEY$BECAME_MNASSA_USER", BECAME_MNASSA_USER_DEFAULT_VALUE))
            }
            iWasRecommended -> {
                val recommented = fromDictionary("$BASE_KEY$RECOMMENDED_YOU_IN_POST", RECOMMENDED_YOU_IN_POST_DEFAULT_VALUE)
                val postText = COMMENTED_POST_I_NEED + item.extra.post?.text
                val text = "$recommented $postText"
                result = getOneSpanText(text, postText, Color.BLACK)
            }
            iWasRecommendedInEvent -> {
                result = SpannableString(fromDictionary("$BASE_KEY$RECOMMENDED_YOU_IN_EVENT", RECOMMENDED_YOU_IN_EVENT_DEFAULT_VALUE))
            }
            userWasRecommended -> {
                result = SpannableString(fromDictionary("$BASE_KEY$RECOMMENDED_YOU", RECOMMENDED_YOU_DEFAULT_VALUE))
                result = SpannableString(fromDictionary("$BASE_KEY$FOR", FOR_DEFAULT_VALUE))
            }
            userWasRecommendedByPost -> {
                result = SpannableString(fromDictionary("$BASE_KEY$ANNOUNCED_YOU", ANNOUNCED_YOU_DEFAULT_VALUE))
            }
            generalPostByAdmin -> {
                result = SpannableString(fromDictionary("$BASE_KEY$GENERAL_POST_BY_ADMIN", GENERAL_POST_BY_ADMIN_DEFAULT_VALUE))
            }
            newEventAttendee -> {
                result = SpannableString(fromDictionary("$BASE_KEY$NEW_EVENT_ATTENDEE", NEW_EVENT_ATTENDEE_DEFAULT_VALUE))
            }
            autoSuggestYouCanHelp -> {
                result = SpannableString(fromDictionary("$BASE_KEY$YOU_CAN_HELP", YOU_CAN_HELP_DEFAULT_VALUE))
            }
            newEventByAdmin -> {
                result = SpannableString(fromDictionary("$BASE_KEY$INVITE_TO_EVENT", INVITE_TO_EVENT_DEFAULT_VALUE))
            }
            invitesNumberChanged -> {
                result = SpannableString(fromDictionary("$BASE_KEY$NUMBER_OF_INVITATIONS", NUMBER_OF_INVITATIONS_DEFAULT_VALUE))
                result = SpannableString(fromDictionary("$BASE_KEY$NUMBER_OF_INVITATIONS_TAIL", NUMBER_OF_INVITATIONS_TAIL_DEFAULT_VALUE))
            }
            connectionsRequestAccepted -> {
                result = SpannableString(fromDictionary("$BASE_KEY$CONNECTION_REQUEST_WAS_ACCEPTED", CONNECTION_REQUEST_WAS_ACCEPTED_DEFAULT_VALUE))
            }
            userWasRecommendedToYou -> {
                val name = getRecommendedName(item)
                val recomend = fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU", USER_WAS_RECOMENDED_TO_YOU_DEFAULT_VALUE)
                val text = "$recomend $name"
                result = getOneSpanText(text, name, Color.BLACK)
            }
            userWasRecommendedInEvent -> {
                result = SpannableString(fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU_IN_EVENT", USER_WAS_RECOMENDED_TO_YOU_IN_EVENT_DEFAULT_VALUE))
                result = SpannableString(fromDictionary("$BASE_KEY$USER_WAS_RECOMENDED_TO_YOU_IN_EVENT_2", USER_WAS_RECOMENDED_TO_YOU_IN_EVENT_2_DEFAULT_VALUE))
            }
            postPromoted -> {
                result = SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", YOU_PROMOTED_YOUR_POST_DEFAULT_VALUE))
            }
            oneDayToExpirationOfPost -> {
                result = SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", YOUR_POST_WILL_EXPIRE_TOMORROW_DEFAULT_VALUE))
            }
            postIsExpired -> {
                result = SpannableString(fromDictionary("${BASE_KEY}_$typeRawValue", YOUR_POST_WAS_EXPIRED_DEFAULT_VALUE))
            }
            eventCancelling -> {
                val canceled = fromDictionary("$BASE_KEY$EVENT_WAS_CANCELLED", EVENT_WAS_CANCELLED_DEFAULT_VALUE)
                val pointsReturns = fromDictionary("${BASE_KEY}$EVENT_WAS_CANCELLED_POINTS_RETURNING", EVENT_WAS_CANCELLED_POINTS_RETURNING_DEFAULT_VALUE)
                val text = "${eventName ?: ""} $canceled ${totalPrice ?: ""} $pointsReturns"
                result = getEventCancellingText(text, eventName ?: "", totalPrice
                        ?: "", Color.BLACK)

            }
            else -> result = SpannableString(item.text)
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
