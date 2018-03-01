package com.mnassa.screen.base

import android.os.Bundle
import android.support.annotation.CallSuper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.github.salomonbrys.kodein.erased
import com.mnassa.App
import com.mnassa.core.BaseViewModelImpl
import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NetworkException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.JobCancellationException
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import timber.log.Timber
import java.util.*

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaViewModelImpl : BaseViewModelImpl(), MnassaViewModel, AndroidInjector<MnassaViewModel, AndroidScope<MnassaViewModel>> {
    final override val injector = KodeinInjector()
    final override val kodeinComponent = super.kodeinComponent
    final override val kodeinScope: AndroidScope<MnassaViewModel> = object : AndroidScope<MnassaViewModel> {
        override fun getRegistry(context: MnassaViewModel): ScopeRegistry = synchronized(CONTEXT_SCOPES) { CONTEXT_SCOPES.getOrPut(context) { ScopeRegistry() } }
        override fun removeFromScope(context: MnassaViewModel): ScopeRegistry? = CONTEXT_SCOPES.remove(context)
    }
    override val errorMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)
    override val isProgressEnabledChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel()

    override fun initializeInjector() {
        val applicationContext = App.context

        val activityModule = Kodein.Module {
            Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@MnassaViewModelImpl)
            import(provideOverridingModule(), allowOverride = true)
        }

        val kodein = Kodein {
            extend(applicationContext.appKodein(), allowOverride = true)
            import(activityModule, allowOverride = true)
        }

        injector.inject(kodein)
    }

    override fun provideOverridingModule() = Kodein.Module {

    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        initializeInjector()
        super.onCreate(savedInstanceState)
    }

    protected suspend fun <T> handleExceptionsSuspend(function: suspend () -> T) {
        try {
            function()
        } catch (e: JobCancellationException) {
            //ignore
            Timber.d(e)
        } catch (e: NetworkDisableException) {
            Timber.d(e)
            errorMessageChannel.send("No internet connection!") //TODO: add text
        } catch (e: NetworkException) {
            Timber.d(e)
            errorMessageChannel.send(e.message)
        } catch (e: Throwable) {
            Timber.e(e)
            throw e
        }
    }

    protected fun <T> handleException(function: suspend () -> T): Job {
        return launchCoroutineUI { handleExceptionsSuspend(function) }
    }

    protected fun showProgress() = asyncUI { isProgressEnabledChannel.send(true) }
    protected fun hideProgress() = asyncUI { isProgressEnabledChannel.send(false) }
    protected suspend fun <T> withProgressSuspend(function: suspend () -> T) {
        showProgress()
        try {
            function()
        } finally {
            hideProgress()
        }
    }
    protected fun <T> withProgress(function: () -> T) {
        showProgress()
        try {
            function()
        } finally {
            hideProgress()
        }
    }



    private companion object {
        private val CONTEXT_SCOPES = WeakHashMap<MnassaViewModel, ScopeRegistry>()
    }
}