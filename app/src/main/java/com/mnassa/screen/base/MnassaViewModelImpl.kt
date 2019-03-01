package com.mnassa.screen.base

import android.os.Bundle
import androidx.annotation.CallSuper
import com.mnassa.App
import com.mnassa.core.BaseViewModelImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.exceptions.internalResolveExceptions
import com.mnassa.exceptions.resolveExceptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ClosedSendChannelException
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

    override val errorMessageChannel: BroadcastChannel<String> = BroadcastChannel(10)
    override val isProgressEnabledChannel: ConflatedBroadcastChannel<Boolean> = ConflatedBroadcastChannel()


    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        kodeinTrigger.trigger()
        super.onCreate(savedInstanceState)
    }

    protected open suspend fun <T> handleExceptionsSuspend(function: suspend CoroutineScope.() -> T): T? {
        return internalResolveExceptions(function) { message ->
            try {
                errorMessageChannel.send(message)
            } catch (_: ClosedSendChannelException) {
            }
        }
    }

    protected open fun showProgress() = resolveExceptions(showErrorMessage = false) { isProgressEnabledChannel.send(true) }
    protected open fun hideProgress() = resolveExceptions { isProgressEnabledChannel.send(false) }
    protected open suspend fun <T> withProgressSuspend(function: suspend () -> T): T {
        showProgress()
        return try {
            function()
        } finally {
            hideProgress()
        }
    }

}