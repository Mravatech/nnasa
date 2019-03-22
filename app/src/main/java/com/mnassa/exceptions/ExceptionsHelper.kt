package com.mnassa.exceptions

import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.core.errorMessagesLive
import com.mnassa.domain.exception.AccountDisabledException
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NotAuthorizedException
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.*
import timber.log.Timber

fun <T : CoroutineScope> T.resolveExceptions(
    showErrorMessage: Boolean = true,
    block: suspend CoroutineScope.() -> Unit
): Job =
    launchWorker {
        internalResolveExceptions(showErrorMessage, block)
    }

suspend fun <T> internalResolveExceptions(
    pushErrorMessage: Boolean = true,
    block: suspend CoroutineScope.() -> T
): T? =
    try {
        coroutineScope {
            block()
        }
    } catch (e: Exception) {
        handleException(e) {
            errorMessagesLive.push(it)
        }
        null
    }

fun handleException(e: Throwable, onMessage: (String) -> Unit) {
    Timber.e("Handled exception", e)

    if (e !is NotAuthorizedException) {
        val message = e.toMessage()
        if (message != null) {
            try {
                onMessage(message)
            } catch (e: Exception) {
                throw RuntimeException("Failed to send error message", e)
            }
        }
    }

    when (e) {
        is NotAuthorizedException -> {
            val interactor = App.context.getInstance<LoginInteractor>()
            if (interactor.isLoggedIn()) {
                // Start a worker job to sign out from
                // current account.
                GlobalScope.launchWorker {
                    interactor.signOut(LogoutReason.NotAuthorized())
                }
            }
        }
        else -> {
        }
    }
}

/**
 * @return error message or `null` if none.
 */
fun Throwable.toMessage(): String? =
    when (this) {
        is NetworkDisableException -> fromDictionary(R.string.error_no_internet)
        is AccountDisabledException -> fromDictionary(R.string.blocked_account_message)
        is CancellationException -> null
        else -> localizedMessage ?: message
    }
        ?.takeUnless { it.isBlank() }
        ?.let {
            val appInfoProvider = App.context.getInstance<AppInfoProvider>()
            if (appInfoProvider.isDebug) {
                """
                    Error message: $it
                    Cause: $cause

                    Stacktrace:
                    ${stackTrace.joinToString()}
                """.trimIndent()
            } else {
                it
            }
        }

