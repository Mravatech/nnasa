package com.mnassa.data.service

import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.mnassa.data.extensions.await
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.retrofit.request.CheckPhoneRequest
import com.mnassa.data.network.exception.FirebaseExceptionHandler
import com.mnassa.data.network.exception.NetworkExceptionHandler
import com.mnassa.data.network.exception.handle
import com.mnassa.data.network.exception.handleNetworkException
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 2/21/2018.
 */
class FirebaseLoginServiceImpl(
        private val authApi: FirebaseAuthApi,
        private val networkErrorHandler: NetworkExceptionHandler,
        private val firebaseExceptionHandler: FirebaseExceptionHandler) : FirebaseLoginService {

    override suspend fun checkPhone(phoneNumber: String, promoCode: String?) {
        authApi.checkPhone(CheckPhoneRequest(phoneNumber, promoCode)).handleNetworkException(networkErrorHandler)
    }

    override fun requestVerificationCode(phoneNumber: String, previousResponse: PhoneVerificationModel?): ReceiveChannel<PhoneVerificationModel> {
        val sendChannel: Channel<PhoneVerificationModel> = RendezvousChannel()

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                async {
                    try {
                        signIn(credential)
                        sendChannel.send(OnVerificationCompleted(phoneNumber, credential))
                        sendChannel.close()
                    } catch (e: Exception) {
                        sendChannel.close(e)
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                sendChannel.close(exception.handle(firebaseExceptionHandler))
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken?) {
                async { sendChannel.send(OnCodeSent(phoneNumber, verificationId, token)) }
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                super.onCodeAutoRetrievalTimeOut(p0)
                sendChannel.close()
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                VERIFY_PHONE_NUMBER_TIMEOUT_SEC,
                TimeUnit.SECONDS,
                { async { it.run() } },
                callback,
                (previousResponse as? OnCodeSent)?.token
        )

        return sendChannel
    }

    override suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
        return EmailAuth()
    }

    override suspend fun signIn(verificationSMSCode: String?, response: PhoneVerificationModel) {
        when {
            verificationSMSCode == null && response is OnVerificationCompleted ->
                signIn(response.credential)
            verificationSMSCode != null && response is OnCodeSent ->
                signIn(PhoneAuthProvider.getCredential(response.verificationId, verificationSMSCode))
        }
    }

    private suspend fun signIn(credential: AuthCredential): AuthResult {
        return try {
            FirebaseAuth.getInstance().signInWithCredential(credential).await()
        } catch (e: FirebaseException) {
            throw e.handle(firebaseExceptionHandler)
        }
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    companion object {
        private const val VERIFY_PHONE_NUMBER_TIMEOUT_SEC = 60L
    }

    @Parcelize
    class OnVerificationCompleted(
            override val phoneNumber: String,
            val credential: AuthCredential,
            override val isVerified: Boolean = true): PhoneVerificationModel

    @Parcelize
    class OnCodeSent(
            override val phoneNumber: String,
            val verificationId: String,
            val token: PhoneAuthProvider.ForceResendingToken?,
            override val isVerified: Boolean = false): PhoneVerificationModel

    @Parcelize
    class EmailAuth(
            override val phoneNumber: String = "",
            override val isVerified: Boolean = true
    ): PhoneVerificationModel
}