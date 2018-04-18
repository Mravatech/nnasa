package com.mnassa.extensions

import android.view.View
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.model.EventDuration
import com.mnassa.domain.model.EventLocationType
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventType
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.event_date.view.*
import timber.log.Timber
import java.text.SimpleDateFormat

/**
 * Created by Peter on 4/17/2018.
 */
val EventModel.formattedType: CharSequence
    get() = when (type) {
        EventType.LECTURE -> fromDictionary(R.string.event_lecture)
        EventType.DISCUSSION -> fromDictionary(R.string.event_discussion)
        EventType.WORKSHOP -> fromDictionary(R.string.event_workshop)
        EventType.EXERCISE -> fromDictionary(R.string.event_exercise)
        EventType.ACTIVITY -> fromDictionary(R.string.event_activity)
        else -> {
            Timber.e("Illegal event type: $type; EventId: $id")
            ""
        }
    }

fun EventModel.bindDate(dateContainer: View) {
    val languageProvider: LanguageProvider = dateContainer.context.getInstance()
    val dateFormatter = SimpleDateFormat("dd MMM", languageProvider.locale)
    val dayOfWeekFormatter = SimpleDateFormat("EEE", languageProvider.locale)

    with(dateContainer) {
        tvDate.text = dateFormatter.format(startAt)
        tvDayOfWeek.text = dayOfWeekFormatter.format(startAt)
    }
}

val EventLocationType.formatted: CharSequence
    get() = when (this) {
        is EventLocationType.Specified -> location.placeName.toString()
        is EventLocationType.NotDefined -> fromDictionary(R.string.event_location_not_defined)
        is EventLocationType.Later -> fromDictionary(R.string.event_location_later)
    }

val EventDuration.formatted: CharSequence get() = ""