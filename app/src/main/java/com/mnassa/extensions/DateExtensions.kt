package com.mnassa.extensions

import android.text.format.Time
import android.widget.TextView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.di.getInstance
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.translation.fromDictionary
import com.mnassa.translation.fromDictionaryPlural
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

/**
 * Created by Peter on 3/15/2018.
 */
fun Date.isTheSameDay(date: Date): Boolean = isTheSameDay(this.time, date.time)
fun Date.isTheSameDay(date: Calendar): Boolean = isTheSameDay(this.time, date.timeInMillis)
fun Date.isTheSameDay(date: Long): Boolean = isTheSameDay(this.time, date)
//
fun Date.isTheSameYear(date: Date): Boolean = isTheSameYear(this.time, date.time)
fun Date.isTheSameYear(date: Calendar): Boolean = isTheSameYear(this.time, date.timeInMillis)
fun Date.isTheSameYear(date: Long): Boolean = isTheSameYear(this.time, date)

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

private fun isTheSameYear(day1: Long, day2: Long): Boolean {
    val time = Time()
    time.set(day1)
    val thenYear = time.year
    time.set(day2)
    return thenYear == time.year
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
    return when {
        TimeUnit.MILLISECONDS.toMinutes(abs(System.currentTimeMillis() - time)) < 1 -> {
            val secondsDiff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.time)
            fromDictionary(R.string._seconds_ago).format(maxOf(secondsDiff, 1))
        }
        isTheSameDay(System.currentTimeMillis()) -> {
            val messages = TimeAgoMessages.Builder().withLocale(locale).build()
            TimeAgo.using(time, messages)
        }
        isTheSameYear(System.currentTimeMillis()) -> {
            val localDateFormat = SimpleDateFormat("dd MMM", locale)
            localDateFormat.format(this)
        }
        else -> formatAsDate()
    }
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

suspend fun TextView.startUpdateTimeJob(time: Date) = startUpdateTimeJob(time, this)

private suspend fun startUpdateTimeJob(time: Date, textView: TextView) {
    textView.stopUpdateTimeJob()
    val textViewRef = textView.asReference()

    textView.tag = coroutineContext.toCoroutineScope().launch(Dispatchers.Main) {
        while (TimeUnit.MILLISECONDS.toMinutes(abs(System.currentTimeMillis() - time.time)) < 1) {
            textViewRef().text = time.toTimeAgo()
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        while (TimeUnit.MILLISECONDS.toHours(abs(System.currentTimeMillis() - time.time)) < 1) {
            textViewRef().text = time.toTimeAgo()
            delay(TimeUnit.MINUTES.toMillis(1))
        }
        while (TimeUnit.MILLISECONDS.toDays(abs(System.currentTimeMillis() - time.time)) < 1) {
            textViewRef().text = time.toTimeAgo()
            delay(TimeUnit.HOURS.toMillis(1))
        }
        textViewRef().text = time.toTimeAgo()
    }
}

fun TextView.stopUpdateTimeJob() {
    val tag = tag
    (tag as? Job)?.cancel()
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
    if (days > 0) result.append("${fromDictionaryPlural(R.string.days_count, days.toInt()).format(days)} ")
    if (hours > 0 || result.isNotEmpty()) result.append("${fromDictionaryPlural(R.string.hours_count, hours.toInt()).format(hours)} ")
    if (minutes > 0 || result.isNotEmpty()) result.append("${fromDictionaryPlural(R.string.minutes_count, minutes.toInt()).format(minutes)} ")
    if (seconds > 0 || result.isEmpty()) result.append("${fromDictionaryPlural(R.string.seconds_count, seconds.toInt()).format(seconds)} ")
    return result.trim().toString()
}