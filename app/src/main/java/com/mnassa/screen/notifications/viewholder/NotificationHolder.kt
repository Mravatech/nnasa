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

        if (item.extra?.author != null) {
            val name = if (item.extra?.author?.accountType == AccountType.PERSONAL) {
                "${item.extra?.author?.personalInfo?.firstName} ${item.extra?.author?.personalInfo?.lastName}"
            } else {
                item.extra?.author?.organizationInfo?.organizationName
            }
            itemView.tvUserName.text = name
        }

        Timber.i(item.type)
        with(itemView) {
            ivUserIcon.avatarRound(item.extra?.author?.avatar)
            tvNotificationInfo.text = getTextByType(item.type, item.extra?.eventName, item.extra?.totalPrice)// + " | " + item.type
            tvNotificationCame.text = item.createdAt.toTimeAgo()
            llNotificationRoot.setOnClickListener(onClickListener)
            llNotificationRoot.tag = this@NotificationHolder
        }
    }

    private fun getTextByType(type: String, eventName: String?, totalPrice: String?): SpannableString {
        var result = SpannableString("")
        var span = SpannableString(result)
        val baseKey = "_notification_text_key"
        val typeRawValue = type
        when (type) {
            privateChatMessage, responseChatMessage -> {
                result = SpannableString(fromDictionary("${baseKey}-"))
            }
            postComment -> {
                result = SpannableString(fromDictionary("${baseKey}_commented_post"))
            }
            connectionRequest -> {
                result = SpannableString(fromDictionary("${baseKey}_requested_connect"))
            }
            postRepost -> {
                result = SpannableString(fromDictionary("${baseKey}_reposted_post"))
            }
            newUserJoined -> {
                result = SpannableString(fromDictionary("${baseKey}_become_mnassa_user"))
            }
            iWasRecommended -> {
                result = SpannableString(fromDictionary("${baseKey}_recommended_you_in_post"))
            }
            iWasRecommendedInEvent -> {
                result = SpannableString(fromDictionary("${baseKey}_recommended_you_in_event"))
            }
            userWasRecommended -> {
                result = SpannableString(fromDictionary("${baseKey}_recommended_you"))
            }
            userWasRecommendedByPost -> {
                result = SpannableString(fromDictionary("${baseKey}_announcedYou"))
            }
            generalPostByAdmin -> {
                result = SpannableString(fromDictionary("${baseKey}_generalPostByAdmin"))
            }
            newEventAttendee -> {
                result = SpannableString(fromDictionary("${baseKey}_newEventAttendee"))
            }
            autoSuggestYouCanHelp -> {
                result = SpannableString(fromDictionary("${baseKey}_youCanHelp"))
            }
            newEventByAdmin -> {
                result = SpannableString(fromDictionary("${baseKey}_inviteToEvent"))
            }
            invitesNumberChanged -> {
                result = SpannableString(fromDictionary("${baseKey}_numberOfInvitations"))
            }
            connectionsRequestAccepted -> {
                result = SpannableString(fromDictionary("${baseKey}_connectionRequestWasAccepted"))
            }
            userWasRecommendedToYou -> {
                result = SpannableString(fromDictionary("${baseKey}_userWasRecommendedToYou"))
            }
            userWasRecommendedInEvent -> {
                result = SpannableString(fromDictionary("${baseKey}_userWasRecommendedToYouInEvent"))
            }
            postPromoted -> {
                result = SpannableString(fromDictionary("${baseKey}_$typeRawValue"))
            }
            oneDayToExpirationOfPost -> {
                result = SpannableString(fromDictionary("${baseKey}_$typeRawValue"))
            }
            postIsExpired -> {
                result = SpannableString(fromDictionary("${baseKey}_$typeRawValue"))
            }
            eventCancelling -> {
                val canceled = fromDictionary("${baseKey}_eventWasCancelled")
                val pointsReturns = fromDictionary("${baseKey}_eventWasCancelledPointsReturning")
                val text = "${eventName ?: ""} $canceled ${totalPrice ?: ""} $pointsReturns"
                result = getEventCancellingText(text, eventName ?: "", totalPrice
                        ?: "", Color.BLACK)

            }
        }
        return result
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


    companion object {
        const val START_SPAN = 0
        fun newInstance(parent: ViewGroup, onClickListener: View.OnClickListener): NotificationHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, parent, false)
            return NotificationHolder(view, onClickListener)
        }
    }
}
