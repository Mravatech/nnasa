package com.mnassa

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.autoAndroidModule
import com.google.firebase.FirebaseApp
import com.mnassa.di.*
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.logging.CrashReportingTree
import com.squareup.leakcanary.LeakCanary
import kotlinx.coroutines.experimental.launch
import timber.log.Timber


/**
 * Created by Peter on 2/20/2018.
 */
class App : Application(), KodeinAware {
    override val kodein: Kodein by Kodein.lazy {
        import(autoAndroidModule(this@App))
        registerAppModules(this)
        bind<Context>() with provider { this@App }
    }

    override fun onCreate() {
        APP_CONTEXT = this
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (instance<AppInfoProvider>().isDebug) {
            if (LeakCanary.isInAnalyzerProcess(this)) return
            LeakCanary.install(this)

            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        } else {
            Timber.plant(CrashReportingTree())
        }

        launch {
            instance<DictionaryInteractor>().handleDictionaryUpdates()
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var APP_CONTEXT: Context
        val context by lazy { APP_CONTEXT }
    }
}