package com.mnassa.extensions

import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.translation.fromDictionary

/**
 * Created by Peter on 3/14/2018.
 */
val ShortAccountModel.formattedFromEvent: CharSequence
    get() {
//        val head = "From event " //TODO: from dictionary
//        val spannable = SpannableString(head + "Some event name")
//        val context = App.context
//        val color = ContextCompat.getColor(requireNotNull(context), R.color.black)
//        spannable.setSpan(ForegroundColorSpan(color), head.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        return spannable
        return ""
    }

val ShortAccountModel.formattedPosition: CharSequence
    get() {
        val ability = abilities.firstOrNull { it.isMain } ?: abilities.firstOrNull() ?: return ""
        return when {
            !ability.name.isNullOrBlank() && !ability.place.isNullOrBlank() -> "${ability.name}${fromDictionary(R.string.invite_at_placeholder)}${ability.place}"
            else -> ability.name ?: ability.place ?: ""
        }
    }