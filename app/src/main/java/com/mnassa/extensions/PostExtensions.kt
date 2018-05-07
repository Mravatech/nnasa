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

/**
 * Created by Peter on 3/19/2018.
 */
fun Double.formatAsMoneySAR(): String {
    return formatAsMoney().toString() + " SAR"
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
            PostType.NEED -> {
                if (text.isNullOrBlank()) return text

                val spannable = SpannableStringBuilder(fromDictionary(R.string.need_prefix))
                spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.append(" ")
                spannable.append(text)
                spannable
            }
            PostType.PROFILE -> {
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

val PostModel.canBeShared: Boolean get() = privacyType != PostPrivacyType.PRIVATE && !this.isMyPost()