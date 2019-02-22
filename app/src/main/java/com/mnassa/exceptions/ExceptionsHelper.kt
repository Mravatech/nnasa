package com.mnassa.exceptions

import android.widget.Toast
import com.google.firebase.FirebaseException
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
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.translation.fromDictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.net.HttpURLConnection

fun <T : CoroutineScope> T.resolveExceptions(
    showErrorMessage: Boolean = true,
    block: suspend () -> Unit
): Job =
    launchWorker {
        internalResolveExceptions(block) { message ->
            /*
            if (showErrorMessage) if (this@resolveExceptions is MnassaViewModel) {
                // Send message to a dedicated error message
                // channel.
                try {
                    errorMessageChannel.send(message)
                } catch (_: Throwable) {
                }
            } else {
                launchUI {
                    Toast.makeText(App.context, message, Toast.LENGTH_LONG).show()
                }
            }
            */
        }
    }

suspend fun <T> internalResolveExceptions(
    block: suspend () -> T,
    onMessage: suspend (String) -> Unit
): T? =
    try {
        coroutineScope {
            block()
        }
    } catch (e: Exception) {
        Timber.e("Handled exception", e)

        val message = e.toMessage()
        if (message != null) {
            //onMessage(message)
        }
/*
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
        }*/

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
        is FirebaseException -> localizedMessage ?: message
        else -> null
    }?.takeUnless { it.isBlank() }
