package com.mnassa

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.autoAndroidModule
import com.google.firebase.FirebaseApp
import com.mnassa.di.*
import com.mnassa.other.CrashReportingTree
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
        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)

            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var APP_CONTEXT: Context
        val context by lazy { APP_CONTEXT }
    }
}