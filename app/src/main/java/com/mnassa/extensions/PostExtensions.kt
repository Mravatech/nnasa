package com.mnassa.extensions

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import com.mnassa.App
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.experimental.channels.consume

/**
 * Created by Peter on 3/19/2018.
 */
fun Double.formatAsMoneySAR(): String {
    return fromDictionary(R.string.points_count).format(formatAsMoney())
}

fun Double.formatAsMoney(): Long {
    return this.toLong()
}

fun LocationPlaceModel?.formatted(): String {
    if (this == null) return ""

    val result = StringBuilder()
    result.append(placeName ?: "")
    if (placeName != null && city != null) {
        result.append(", ")
    }
    result.append(city ?: "")
    return result.toString()
}

val PostModel.formattedText: CharSequence?
    get() {

        return when (type) {
            is PostType.NEED -> {
                if (text.isNullOrBlank()) return text

                val spannable = SpannableStringBuilder(fromDictionary(R.string.need_prefix))
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.append(" ")
                spannable.append(text)
                spannable
            }
            is PostType.PROFILE -> {
                this as RecommendedProfilePostModel

                val spannable = SpannableStringBuilder(fromDictionary(R.string.recommend_prefix))
                spannable.append(" ")
                val nameStart = spannable.length
                spannable.append(this.recommendedProfile.formattedName)
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(App.context, R.color.accent)), nameStart, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (!text.isNullOrBlank()) {
                    spannable.append(", ")
                }
                spannable.append(text ?: "")
                spannable
            }
            is PostType.OFFER -> {
                if (text.isNullOrBlank()) return text

                val spannable = SpannableStringBuilder(fromDictionary(R.string.offer_prefix))
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.append(" ")
                spannable.append(text)
                spannable
            }
            else -> text
        }
    }

fun PostModel.isMyPost(): Boolean = author.id == App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull()

val PostModel.isRepost: Boolean get() = originalId != id


suspend fun PostModel.markAsOpened() {
    val postInteractor = App.context.getInstance<PostsInteractor>()
    postInteractor.onItemOpened(this)
}

fun ImageView.image(postAttachment: PostAttachment, crop: Boolean = true) {
    (parent as? View)?.findViewById<View>(R.id.ivPlay)?.isInvisible = postAttachment is PostAttachment.PostPhotoAttachment

    when (postAttachment) {
        is PostAttachment.PostPhotoAttachment -> image(postAttachment.photoUrl, crop)
        is PostAttachment.PostVideoAttachment -> image(postAttachment.previewUrl, crop)
    }
}

suspend fun OfferPostModel.getBoughtItemsCount(): Int = 0

val PostModel.canBeShared: Boolean get() = privacyType !is PostPrivacyType.PRIVATE && !isMyPost()

suspend fun PostModel?.canBePromoted(): Boolean {
    if (this?.privacyType is PostPrivacyType.WORLD) return false
    if (this?.isMyPost() == false) return false

    val userProfileInteractor: UserProfileInteractor = App.context.getInstance()
    val permissions = userProfileInteractor.getPermissions().consume { receive() }

    return when (this?.type) {
        is PostType.NEED -> permissions.canPromoteNeedPost
        is PostType.OFFER -> permissions.canPromoteOfferPost
        is PostType.GENERAL -> permissions.canPromoteGeneralPost
        is PostType.PROFILE -> permissions.canPromoteAccountPost
        is PostType.INFO -> false
        is PostType.OTHER -> false
        else -> false
    }
}

suspend fun PostModel.getPromotionPrice(): Long {
    return App.context.getInstance<PostsInteractor>().getPromotePostPrice()
}