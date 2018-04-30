package com.mnassa.extensions

import android.text.format.Time
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.mnassa.App
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.translation.fromDictionary
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 3/15/2018.
 */
fun Date.isTheSameDay(date: Date): Boolean = isTheSameDay(this.time, date.time)

fun Date.isTheSameDay(date: Calendar): Boolean = isTheSameDay(this.time, date.timeInMillis)
fun Date.isTheSameDay(date: Long): Boolean = isTheSameDay(this.time, date)

private fun isTheSameDay(day1: Long, day2: Long): Boolean {
    val time = Time()
    time.set(day1)

    val thenYear = time.year
    val thenMonth = time.month
    val thenMonthDay = time.monthDay

    time.set(day2)
    return thenYear == time.year
            && thenMonth == time.month
            && thenMonthDay == time.monthDay
}

fun Date.getStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.getEndOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

fun Date.toTimeAgo(): CharSequence {
    val locale = App.context.getInstance<LanguageProvider>().locale
    val messages = TimeAgoMessages.Builder().withLocale(locale).build()
    return TimeAgo.using(time, messages)
}


fun Date.formatAsTime(): CharSequence {
    val languageProvider: LanguageProvider = App.context.getInstance()
    val localDateFormat = SimpleDateFormat("hh:mm a", languageProvider.locale)
    return localDateFormat.format(this)
}

fun Date.formatAsDate(): CharSequence {
    val languageProvider: LanguageProvider = App.context.getInstance()
    val localDateFormat = SimpleDateFormat("dd MMM yyyy", languageProvider.locale)
    return localDateFormat.format(this)
}

fun Date.formatAsDateTime(): CharSequence {
    val languageProvider: LanguageProvider = App.context.getInstance()
    val localDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", languageProvider.locale)
    return localDateFormat.format(this)
}


fun Long.formatAsDuration(): CharSequence {
    var value = this
    val days = TimeUnit.MILLISECONDS.toDays(value)
    value -= TimeUnit.DAYS.toMillis(days)
    val hours = TimeUnit.MILLISECONDS.toHours(value)
    value -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(value)
    value -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(value)
    value -= TimeUnit.SECONDS.toMillis(seconds)

    val result = StringBuilder()
    if (days > 0) result.append("${fromDictionary(R.string.days_count).format(days)} ")
    if (hours > 0 || result.isNotEmpty()) result.append("${fromDictionary(R.string.hours_count).format(hours)} ")
    if (minutes > 0 || result.isNotEmpty()) result.append("${fromDictionary(R.string.minutes_count).format(minutes)} ")
    if (seconds > 0 || result.isEmpty()) result.append("${fromDictionary(R.string.seconds_count).format(seconds)} ")
    return result.trim().toString()
}