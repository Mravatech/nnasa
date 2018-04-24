package com.mnassa.screen.base

import android.os.Bundle
import android.support.annotation.CallSuper
import android.util.Log
import com.google.firebase.FirebaseException
import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.BaseViewModelImpl
import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.exception.NotAuthorizedException
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.JobCancellationException
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaViewModelImpl : BaseViewModelImpl(), KodeinAware, MnassaViewModel {
    override val kodeinTrigger = KodeinTrigger()
    override val kodein: Kodein = Kodein.lazy {
        val parentKodein by closestKodein(requireNotNull(App.context))
        extend(parentKodein, allowOverride = true)
    }
    private val appInfoProvider: AppInfoProvider by instance()

    override val errorMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)
    override val isProgressEnabledChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel()


    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        kodeinTrigger.trigger()
        super.onCreate(savedInstanceState)
    }

    protected open suspend fun <T> handleExceptionsSuspend(function: suspend () -> T): T? {
        var result: T? = null
        try {
            result = function()
        } catch (e: JobCancellationException) {
            //ignore
            Timber.d(e)
        } catch (e: NetworkDisableException) {
            Timber.d(e)
            errorMessageChannel.send(fromDictionary(R.string.error_no_internet))
        } catch (e: NotAuthorizedException) {
            Timber.e(e)
            val loginInteractor by kodein.instance<LoginInteractor>()
            loginInteractor.signOut()
        } catch (e: NetworkException) {
            Timber.d(e)
            if (appInfoProvider.isDebug) {
                val message = "${e.message}\n${Log.getStackTraceString(e.cause)}"
                errorMessageChannel.send(message)
            }
        } catch (e: FirebaseException) {
            Timber.e(e)
            val message = e.localizedMessage ?: e.message
            message?.let { errorMessageChannel.send(it) }
        } catch (e: Throwable) {
            Timber.e(e)
            throw e
        }
        return result
    }

    open fun <T> handleException(function: suspend () -> T): Job {
        return launchCoroutineUI { handleExceptionsSuspend(function) }
    }

    protected open fun showProgress() = asyncUI { isProgressEnabledChannel.send(true) }
    protected open fun hideProgress() = asyncUI { isProgressEnabledChannel.send(false) }
    protected open suspend fun <T> withProgressSuspend(function: suspend () -> T) {
        showProgress()
        try {
            function()
        } finally {
            hideProgress()
        }
    }

    protected open fun <T> withProgress(function: () -> T) {
        showProgress()
        try {
            function()
        } finally {
            hideProgress()
        }
    }

}