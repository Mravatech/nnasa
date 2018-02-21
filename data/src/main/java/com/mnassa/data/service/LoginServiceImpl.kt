package com.mnassa.data.service

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mnassa.domain.service.LoginService
import kotlinx.coroutines.experimental.async
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/21/2018.
 */
class LoginServiceImpl : LoginService {

    override suspend fun requestVerificationCode(phoneNumber: String): LoginService.VerificationCodeResponse {
        return suspendCoroutine { continuation ->
            val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    continuation.resume(VerificationCodeResponseImpl.OnVerificationCompleted(credential))
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    continuation.resumeWithException(exception) //TODO: use custom exceptions
                }

                override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                    continuation.resume(VerificationCodeResponseImpl.OnCodeSent(verificationId, token))
                }

                override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                    super.onCodeAutoRetrievalTimeOut(p0)
                    continuation.resumeWithException(IllegalStateException(p0)) //TODO: use custom exceptions
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
        }
    }

    override suspend fun login() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private sealed class VerificationCodeResponseImpl(override val isVerificationNeeded: Boolean) : LoginService.VerificationCodeResponse {
        data class OnVerificationCompleted(val credential: PhoneAuthCredential) : VerificationCodeResponseImpl(false)
        data class OnCodeSent(val verificationId: String?, val token: PhoneAuthProvider.ForceResendingToken?) : VerificationCodeResponseImpl(true)
    }

}