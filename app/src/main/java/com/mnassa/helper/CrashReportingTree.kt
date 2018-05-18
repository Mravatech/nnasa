package com.mnassa.helper

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.mnassa.domain.interactor.UserProfileInteractor
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class CrashReportingTree(private val userProfileInteractor: UserProfileInteractor) : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        Crashlytics.log(priority, tag, "$message; " +
                "accountId = [${userProfileInteractor.getAccountIdOrNull()}]; " +
                "TRACE = ${Log.getStackTraceString(t)}")
    }
}