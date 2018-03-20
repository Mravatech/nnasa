package com.mnassa.extensions

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.mnassa.R
import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.Post
import com.mnassa.domain.model.PostType
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/19/2018.
 */
fun Double.formatAsMoney(): String {
    val formatted = (this * 100).toLong() / 100L
    return formatted.toString() + " SAR"
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

val Post.formattedText: CharSequence?
    get() {
        if (text.isNullOrBlank()) return text
        return if (type == PostType.NEED) {
            val spannable = SpannableStringBuilder(fromDictionary(R.string.need_prefix))
            spannable.setSpan(StyleSpan(Typeface.BOLD), 0, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.append(" ")
            spannable.append(text)
            spannable
        } else text

    }