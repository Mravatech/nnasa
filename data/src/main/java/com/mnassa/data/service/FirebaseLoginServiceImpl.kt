package com.mnassa.data.service

import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.mnassa.core.addons.launchWorker
import com.mnassa.data.extensions.await
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.retrofit.request.CheckPhoneRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.PhoneValidationResult
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 2/21/2018.
 */
class FirebaseLoginServiceImpl(
        private val authApi: FirebaseAuthApi,
        private val exceptionHandler: ExceptionHandler,
        private val context: Context) : FirebaseLoginService {

    private val verifyPhoneNumberTimeoutSec by lazy { context.resources.getInteger(com.mnassa.data.R.integer.validation_code_resend_delay_seconds).toLong() }

    override suspend fun checkPhone(phoneNumber: String, promoCode: String?): PhoneValidationResult {
        Timber.d("MNSA_LOGIN checkPhone $phoneNumber with promo $promoCode")
        val useCustomAuth = authApi.checkPhone(CheckPhoneRequest(phoneNumber, promoCode)).handleException(exceptionHandler).data?.customAuth ?: false
        return PhoneValidationResult(useCustomAuth = useCustomAuth)
    }

    override fun requestVerificationCode(phoneNumber: String, previousResponse: PhoneVerificationModel?): ReceiveChannel<PhoneVerificationModel> {
        val sendChannel: Channel<PhoneVerificationModel> = RendezvousChannel()

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Timber.d("MNSA_LOGIN requestVerificationCode -> onVerificationCompleted -> ${credential.smsCode}")
                launchWorker {
                    try {
                        signIn(credential)
                        sendChannel.send(OnVerificationCompleted(phoneNumber))
                        sendChannel.close()
                    } catch (e: Exception) {
                        sendChannel.close(e)
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                sendChannel.close(exceptionHandler.handle(exception))
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken?) {
                Timber.d("MNSA_LOGIN requestVerificationCode -> onCodeSent")
                launchWorker {
                    sendChannel.send(OnCodeSent(
                            phoneNumber = phoneNumber,
                            verificationId = verificationId,
                            token = token
                    ))
                }
            }
        }

        Timber.d("MNSA_LOGIN requestVerificationCode -> opening channel for $phoneNumber")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                verifyPhoneNumberTimeoutSec,
                TimeUnit.SECONDS,
                { it.run() },
                callback,
                (previousResponse as? OnCodeSent)?.token
        )

        return sendChannel
    }

    override suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await(exceptionHandler)
        return EmailAuth()
    }

    override suspend fun signIn(verificationSMSCode: String?, response: PhoneVerificationModel) {
        Timber.d("MNSA_LOGIN signIn verificationSMSCode $verificationSMSCode, response: $response")
        when {
            verificationSMSCode == null && response is OnVerificationCompleted -> { /* do nothing */
            }
            verificationSMSCode != null && response is OnCodeSent ->
                signIn(PhoneAuthProvider.getCredential(response.verificationId, verificationSMSCode))
        }
    }

    private suspend fun signIn(credential: AuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential).await(exceptionHandler)
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
        withContext(DefaultDispatcher) { FirebaseInstanceId.getInstance().deleteInstanceId() }
    }
}