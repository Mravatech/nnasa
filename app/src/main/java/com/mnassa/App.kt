package com.mnassa

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.autoAndroidModule
import com.github.salomonbrys.kodein.lazy
import com.mnassa.di.*
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber


/**
 * Created by Peter on 2/20/2018.
 */
class App : Application(), KodeinAware {
    override val kodein: Kodein by Kodein.lazy {
        import(autoAndroidModule(this@App))
        registerAppModules(this)
    }

    override fun onCreate() {
        APP_CONTEXT = this
        super.onCreate()

        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)

            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

//            FakeCrashLibrary.log(priority, tag, message)
//            if (t != null) {
//                if (priority == Log.ERROR) {
//                    FakeCrashLibrary.logError(t)
//                } else if (priority == Log.WARN) {
//                    FakeCrashLibrary.logWarning(t)
//                }
//            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var APP_CONTEXT: Context
        val context by lazy { APP_CONTEXT }
    }
}