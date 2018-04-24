package com.mnassa.helper

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        Crashlytics.log(priority, tag, "$message; TRACE = ${Log.getStackTraceString(t)}")
    }
}