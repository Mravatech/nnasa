package com.mnassa.core

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/20/2018.
 */
abstract class BaseViewModelImpl : ViewModel(), BaseViewModel,
    SubscriptionContainer by SubscriptionsContainerDelegate(
        dispatcher = Dispatchers.Main + CoroutineExceptionHandler { context, throwable ->
            // By default show all error messages we got
            // here.
            errorHandler(throwable, errorMessagesLive::push)
        },
        jobFactory = { SupervisorJob() }
    ) {

    override val setupErrorChannel: BroadcastChannel<String> = BroadcastChannel(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        openSubscriptionsScope()
        launchSetup()
    }

    private fun launchSetup() {
        val context = Dispatchers.Main + CoroutineExceptionHandler { context, throwable ->
            errorHandler(throwable) { message  ->
                // Send a "setup failed" message to
                // a view, so it can propose user to
                // restart itself.
                launch {
                    setupErrorChannel.send(message)
                }
            }
        }

        launch(context) {
            onSetup(this)
        }
    }

    override fun onSetup(setupScope: CoroutineScope) {
    }

    override fun saveInstanceState(outBundle: Bundle) {

    }

    @CallSuper
    final override fun onCleared() {
        closeSubscriptionsScope()
        super.onCleared()
    }
}