package com.mnassa.extensions

import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import com.mnassa.R
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/15/2018.
 */
fun TextView.boldPrefix(prefix: String, text: String): TextView {
    val str = SpannableStringBuilder(prefix)
    val start = 0
    val end = str.length
    str.append(" ")
    str.append(text)
    str.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    setText(str)

    return this
}

fun TextView.boldPrefix(prefixStringId: Int, text: String): TextView =
        boldPrefix(context.getString(prefixStringId), text)

fun TextView.setHeaderWithCounter(dictionaryResId: Int, counterValue: Int) {
    val head = "${fromDictionary(dictionaryResId)}  "
    val spannable = SpannableString(head + counterValue.toString())
    val color = ContextCompat.getColor(context, R.color.coolGray)
    spannable.setSpan(ForegroundColorSpan(color), head.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = spannable
}