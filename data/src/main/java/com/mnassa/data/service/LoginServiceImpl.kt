package com.mnassa.data.service

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.service.LoginService
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/21/2018.
 */
class LoginServiceImpl : LoginService {

    override suspend fun requestVerificationCode(phoneNumber: String, previousResponse: LoginService.VerificationCodeResponse?): ReceiveChannel<LoginService.VerificationCodeResponse> {
        val sendChannel: Channel<LoginService.VerificationCodeResponse> = RendezvousChannel()

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                async {
                    try {
                        signIn(credential)
                        sendChannel.send(VerificationCodeResponseImpl.OnVerificationCompleted(phoneNumber, credential))
                        sendChannel.close()
                    } catch (e: Exception) {
                        sendChannel.close(e)
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                sendChannel.close(exception)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken?) {
                async { sendChannel.send(VerificationCodeResponseImpl.OnCodeSent(phoneNumber, verificationId, token)) }
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
                callback,
                (previousResponse as? VerificationCodeResponseImpl.OnCodeSent)?.token
        )

        return sendChannel
    }

    override suspend fun signIn(verificationSMSCode: String, response: LoginService.VerificationCodeResponse) {
        val credential = PhoneAuthProvider.getCredential((response as VerificationCodeResponseImpl.OnCodeSent).verificationId, verificationSMSCode)
        signIn(credential)
    }

    private suspend fun signIn(credential: PhoneAuthCredential) {
        suspendCoroutine<AuthResult> { continuation ->
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                when {
                    it.isSuccessful -> continuation.resume(it.result)
                    it.exception is FirebaseAuthInvalidCredentialsException -> continuation.resumeWithException(LoginInteractor.InvalidVerificationCode())
                    else -> continuation.resumeWithException(it.exception
                            ?: IllegalStateException())
                }
            }
        }
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private sealed class VerificationCodeResponseImpl(override val isVerificationNeeded: Boolean) : LoginService.VerificationCodeResponse {

        data class OnVerificationCompleted(override val phoneNumber: String, val credential: PhoneAuthCredential) : VerificationCodeResponseImpl(false) {
            constructor(parcel: Parcel) : this(parcel.readString(), parcel.readParcelable<PhoneAuthCredential>(PhoneAuthCredential::class.java.classLoader))
            override fun writeToParcel(dest: Parcel, flags: Int) {
                dest.writeString(phoneNumber)
                dest.writeParcelable(credential, 0)
            }

            override fun describeContents(): Int = 0

            companion object CREATOR : Parcelable.Creator<OnVerificationCompleted> {
                override fun createFromParcel(parcel: Parcel): OnVerificationCompleted = OnVerificationCompleted(parcel)
                override fun newArray(size: Int): Array<OnVerificationCompleted?> = arrayOfNulls(size)

            }
        }
        data class OnCodeSent(override val phoneNumber: String, val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken?) : VerificationCodeResponseImpl(true) {
            constructor(parcel: Parcel) : this(
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readParcelable(PhoneAuthProvider.ForceResendingToken::class.java.classLoader))

            override fun writeToParcel(dest: Parcel, flags: Int) {
                dest.writeString(phoneNumber)
                dest.writeString(verificationId)
                dest.writeParcelable(token, 0)
            }

            override fun describeContents(): Int = 0

            companion object CREATOR : Parcelable.Creator<OnCodeSent> {
                override fun createFromParcel(parcel: Parcel): OnCodeSent {
                    return OnCodeSent(parcel)
                }

                override fun newArray(size: Int): Array<OnCodeSent?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}