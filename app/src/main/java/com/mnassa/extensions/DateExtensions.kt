package com.mnassa.extensions

import android.text.format.Time
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mnassa.App
import com.mnassa.domain.other.LanguageProvider
import java.util.*

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

fun Date.toTimeAgo(): String {
    val locale = App.context.appKodein().instance<LanguageProvider>().locale
    val messages = TimeAgoMessages.Builder().withLocale(locale).build()
    return TimeAgo.using(time, messages)
}