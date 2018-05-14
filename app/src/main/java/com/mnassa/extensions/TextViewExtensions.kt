package com.mnassa.extensions

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.mnassa.R
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/15/2018.
 */

fun TextView.setHeaderWithCounter(dictionaryResId: Int, counterValue: Int) {
    val head = "${fromDictionary(dictionaryResId)}  "
    val spannable = SpannableString(head + counterValue.toString())
    val color = ContextCompat.getColor(context, R.color.gray_cool)
    spannable.setSpan(ForegroundColorSpan(color), head.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannable
}

fun TextView.setTextWithOneSpanText(entireText: String, spanText: String, color: Int) {
    val span = SpannableString(entireText)
    val pointsReturnsPosition = entireText.indexOf(spanText)
    span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = span
}
