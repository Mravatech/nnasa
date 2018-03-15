package com.mnassa.extensions

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView

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