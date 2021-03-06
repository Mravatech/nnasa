package com.mnassa.screen.base

import android.os.Bundle
import androidx.annotation.CallSuper
import com.mnassa.App
import com.mnassa.core.BaseViewModelImpl
import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.exceptions.internalResolveExceptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinTrigger
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

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

    override val isProgressEnabledChannel: ConflatedBroadcastChannel<ProgressEvent> = ConflatedBroadcastChannel()


    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        kodeinTrigger.trigger()
        super.onCreate(savedInstanceState)
    }


    protected open suspend fun <T> handleExceptionsSuspend(function: suspend CoroutineScope.() -> T): T? {
        return internalResolveExceptions(true, function)
    }



    protected open fun showProgress(hideKeyboard: Boolean = ShowProgressEvent.HIDE_KEYBOARD) =
        launchWorkerNoExceptions {
            isProgressEnabledChannel.send(ShowProgressEvent(hideKeyboard))
        }

    protected open fun hideProgress() =
        launchWorker {
            isProgressEnabledChannel.send(HideProgressEvent())
        }

    protected open suspend fun <T> withProgressSuspend(hideKeyboard: Boolean = ShowProgressEvent.HIDE_KEYBOARD, function: suspend () -> T): T {
        showProgress(hideKeyboard)
        return try {
            function()
        } finally {
            hideProgress()
        }
    }

}