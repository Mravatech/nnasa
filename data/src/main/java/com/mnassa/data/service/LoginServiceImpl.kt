package com.mnassa.data.service

import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mnassa.domain.service.LoginService
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/21/2018.
 */
class LoginServiceImpl : LoginService {

    override suspend fun requestVerificationCode(phoneNumber: String): Channel<LoginService.VerificationCodeResponse> {
        val sendChannel: Channel<LoginService.VerificationCodeResponse> = RendezvousChannel()

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                async {
                    signIn(credential)
                    sendChannel.send(VerificationCodeResponseImpl.OnVerificationCompleted(credential))
                    sendChannel.close()
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                sendChannel.close(exception)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken?) {
                async { sendChannel.send(VerificationCodeResponseImpl.OnCodeSent(verificationId, token)) }
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                super.onCodeAutoRetrievalTimeOut(p0)
                sendChannel.close(IllegalStateException(p0))
            }
        }

        val executor = Executor { async { it.run() } }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                executor,
                callback
        )

        return sendChannel
    }

    override suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse) {
        val credential = PhoneAuthProvider.getCredential((response as VerificationCodeResponseImpl.OnCodeSent).verificationId, verificationSMSCode)
        signIn(credential)
    }

    private suspend fun signIn(credential: PhoneAuthCredential) {
        val authResult = suspendCoroutine<AuthResult> { continuation ->
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                when {
                    it.isSuccessful -> continuation.resume(it.result)
                    else -> continuation.resumeWithException(it.exception
                            ?: IllegalStateException())
                }
            }
        }

        Timber.i("User ${authResult.user.displayName} and id ${authResult.user.uid} was signed in!")
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private sealed class VerificationCodeResponseImpl(override val isVerificationNeeded: Boolean) : LoginService.VerificationCodeResponse {
        data class OnVerificationCompleted(val credential: PhoneAuthCredential) : VerificationCodeResponseImpl(false)
        data class OnCodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken?) : VerificationCodeResponseImpl(true)
    }

}