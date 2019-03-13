package com.mnassa

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.Stetho
import com.github.piasy.biv.BigImageViewer
import com.google.firebase.FirebaseApp
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.di.getInstance
import com.mnassa.di.registerAppModules
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NetworkInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.helper.CrashReportingTree
import com.mnassa.utils.FirebaseBigImageLoader
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class App : MultiDexApplication(), KodeinAware, LifecycleObserver,
    SubscriptionContainer by SubscriptionsContainerDelegate(jobFactory = { SupervisorJob() }) {

    override val kodein: Kodein = Kodein.lazy {
        import(androidModule(this@App))
        registerAppModules(this)
        bind<Context>() with provider { this@App }
    }

    override fun onCreate() {
        APP_CONTEXT = this
        super.onCreate()
        val appInfoProvider = getInstance<AppInfoProvider>()
        if (!appInfoProvider.isDebug) FirebaseApp.initializeApp(this)
        if (appInfoProvider.isDebug) {
            Timber.plant(Timber.DebugTree())
            Stetho.initializeWithDefaults(this)
        } else {
            Timber.plant(CrashReportingTree(getInstance()))
        }
        Fabric.with(this, Crashlytics())

        BigImageViewer.initialize(FirebaseBigImageLoader.with(this))

        getInstance<NetworkInteractor>().register()

        Timber.e("appId: ${appInfoProvider.applicationId}")
        Timber.e("packageName: $packageName")
        Timber.e("isDebug: ${appInfoProvider.isDebug}")
        Timber.e("endpoint: ${appInfoProvider.endpoint}")
        Timber.e("versionCode: ${appInfoProvider.versionCode}")
        Timber.e("versionName: ${appInfoProvider.versionName}")

        // Listen to the app lifecycle
        // changes.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        Timber.i("App is now foreground")
        openSubscriptionsScope()

        // Launch background tasks on foreground app
        // scope.
        getInstance<DictionaryInteractor>().apply {
            handleDictionaryUpdates()
        }
        getInstance<LoginInteractor>().apply {
            handleUserStatus()
            handleAccountStatus()
            handleAccountRefresh()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        closeSubscriptionsScope()
        Timber.i("App is now background")
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