package com.mnassa.helper

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.UserProfileInteractor
import timber.log.Timber

/**
 * Created by Peter on 2/23/2018.
 */
class CrashReportingTree(private val appContext: Context) : Timber.Tree() {

    private val userProfileInteractor by lazy { appContext.getInstance<UserProfileInteractor>() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        Crashlytics.log(priority, tag, "$message; " +
                "accountId = [${userProfileInteractor.getAccountIdOrNull()}]; " +
                "TRACE = ${Log.getStackTraceString(t)}")
    }
}