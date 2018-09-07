package com.mnassa

import android.annotation.SuppressLint
import android.content.Context
import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.di.registerAppModules
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NetworkInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.helper.CrashReportingTree
import io.fabric.sdk.android.Fabric
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class App : MultiDexApplication(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        import(androidModule(this@App))
        registerAppModules(this)
        bind<Context>() with provider { this@App }
    }

    override fun onCreate() {
        APP_CONTEXT = this
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val appInfoProvider = getInstance<AppInfoProvider>()
        if (appInfoProvider.isDebug) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        } else {
            Timber.plant(CrashReportingTree(getInstance()))
        }
        Fabric.with(this, Crashlytics())

        launchWorker {
            getInstance<DictionaryInteractor>().handleDictionaryUpdates()
        }

        launchWorker {
            getInstance<LoginInteractor>().handleUserStatus()
        }

        launchWorker {
            getInstance<LoginInteractor>().handleAccountStatus()
        }

        getInstance<NetworkInteractor>().register()

        Timber.e("appId: ${appInfoProvider.applicationId}")
        Timber.e("packageName: $packageName")
        Timber.e("isDebug: ${appInfoProvider.isDebug}")
        Timber.e("endpoint: ${appInfoProvider.endpoint}")
        Timber.e("versionCode: ${appInfoProvider.versionCode}")
        Timber.e("versionName: ${appInfoProvider.versionName}")
    }

    override fun onTerminate() {
        getInstance<NetworkInteractor>().unregister()
        super.onTerminate()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var APP_CONTEXT: Context
        val context by lazy { APP_CONTEXT }
    }
}