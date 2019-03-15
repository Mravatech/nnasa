package com.mnassa.exceptions

import android.widget.Toast
import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.domain.exception.AccountDisabledException
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.exception.NotAuthorizedException
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.screen.base.MnassaController
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.HttpURLConnection

fun <T : CoroutineScope> T.resolveExceptions(
    showErrorMessage: Boolean = true,
    block: suspend CoroutineScope.() -> Unit
): Job =
    launchWorker {
        internalResolveExceptions(block) { message ->
            if (showErrorMessage) {
                val viewModel = this@resolveExceptions as? MnassaViewModel
                    ?: (this@resolveExceptions as? MnassaController<*>)
                        ?.viewModel as? MnassaViewModel
                if (viewModel != null) {
                    // Send message to a dedicated error message
                    // channel.
                    try {
                        viewModel.errorMessageChannel.send(message)
                    } catch (_: Throwable) {
                    }
                } else {
                    launchUI {
                        Toast.makeText(App.context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

suspend fun <T> internalResolveExceptions(
    block: suspend CoroutineScope.() -> T,
    onMessage: suspend (String) -> Unit
): T? =
    try {
        coroutineScope {
            block()
        }
    } catch (e: Exception) {
        Timber.e("Handled exception", e)

        if (e !is NotAuthorizedException) {
            val message = e.toMessage()
            if (message != null) {
                onMessage(message)
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

        null
    }

/**
 * @return error message or `null` if none.
 */
fun Throwable.toMessage(): String? =
    when (this) {
        is NetworkDisableException -> fromDictionary(R.string.error_no_internet)
        is AccountDisabledException -> fromDictionary(R.string.blocked_account_message)
        is NetworkException -> {
            // Ignore bad request errors
            // for now.
            if (code != HttpURLConnection.HTTP_BAD_REQUEST) {
                localizedMessage ?: message
            } else {
                null
            }
        }
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

