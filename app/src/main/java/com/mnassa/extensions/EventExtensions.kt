package com.mnassa.extensions

import android.view.View
import com.mnassa.App
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
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
        is EventType.LECTURE -> fromDictionary(R.string.event_lecture)
        is EventType.DISCUSSION -> fromDictionary(R.string.event_discussion)
        is EventType.WORKSHOP -> fromDictionary(R.string.event_workshop)
        is EventType.EXERCISE -> fromDictionary(R.string.event_exercise)
        is EventType.ACTIVITY -> fromDictionary(R.string.event_activity)
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
        is EventLocationType.Specified -> {
            val result = StringBuilder()
            val placeName = location.placeName?.toString()
            val city = location.city?.toString()
            if (placeName != null) {
                result.append(placeName)
            }
            if (placeName != city && city != null) {
                if (result.isNotEmpty()) result.append("\n")
                result.append(city)
            }
            result.toString()
        }
        is EventLocationType.NotDefined -> fromDictionary(R.string.event_location_not_defined)
        is EventLocationType.Later -> fromDictionary(R.string.event_location_later)
    }

val EventDuration.formatted: CharSequence
    get() = "$value " + when (this) {
        is EventDuration.Minute -> fromDictionary(R.string.event_duration_minute)
        is EventDuration.Hour -> fromDictionary(R.string.event_duration_hour)
        is EventDuration.Day -> fromDictionary(R.string.event_duration_day)
    }

val EventType.formatted: CharSequence
    get() = when (this) {
        is EventType.LECTURE -> fromDictionary(R.string.event_lecture)
        is EventType.DISCUSSION -> fromDictionary(R.string.event_discussion)
        is EventType.WORKSHOP -> fromDictionary(R.string.event_workshop)
        is EventType.EXERCISE -> fromDictionary(R.string.event_exercise)
        is EventType.ACTIVITY -> fromDictionary(R.string.event_activity)
        else -> {
            Timber.e("Illegal event type $this")
            ""
        }
    }

val EventStatus.formatted: CharSequence
    get() = when(this) {
        is EventStatus.ANNULED -> fromDictionary(R.string.event_status_annulled)
        is EventStatus.OPENED -> fromDictionary(R.string.event_status_opened)
        is EventStatus.CLOSED -> fromDictionary(R.string.event_status_closed)
        is EventStatus.SUSPENDED -> fromDictionary(R.string.event_status_suspended)
        else -> {
            Timber.e("Illegal event status $this")
            ""
        }
    }

suspend fun EventModel.canBuyTickets(): Boolean {
    return App.context.getInstance<EventsInteractor>().canBuyTicket(id)
}

suspend fun EventModel.getBoughtTicketsCount(): Long {
    return App.context.getInstance<EventsInteractor>().getBoughtTicketsCount(id)
}

val EventModel.isFree: Boolean get() = price == 0L

fun EventModel.isMyEvent(): Boolean = author.id == App.context.getInstance<UserProfileInteractor>().getAccountIdOrNull()