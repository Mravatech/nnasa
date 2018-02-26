package com.mnassa.data.service

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.mnassa.data.extensions.await
import com.mnassa.data.network.api.FirebaseAuthApi
import com.mnassa.data.network.bean.retrofit.CheckPhoneRequest
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 2/21/2018.
 */
class FirebaseLoginServiceImpl(private val authApi: FirebaseAuthApi) : FirebaseLoginService {

    override suspend fun checkPhone(phoneNumber: String, promoCode: String?) {
        try {
            authApi.checkPhone(CheckPhoneRequest(phoneNumber, promoCode)).await()
        } catch (e: HttpException) {
            ---- TODO
            e.response().errorBody()?.
        }
    }

    override suspend fun requestVerificationCode(phoneNumber: String, previousResponse: PhoneVerificationModel?): ReceiveChannel<PhoneVerificationModel> {
        val sendChannel: Channel<PhoneVerificationModel> = RendezvousChannel()

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

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                { async { it.run() } },
                callback,
                (previousResponse as? VerificationCodeResponseImpl.OnCodeSent)?.token
        )

        return sendChannel
    }

    override suspend fun signIn(verificationSMSCode: String?, response: PhoneVerificationModel) {
        when {
            verificationSMSCode == null && response is VerificationCodeResponseImpl.OnVerificationCompleted ->
                    signIn(response.credential)
            verificationSMSCode != null && response is VerificationCodeResponseImpl.OnCodeSent ->
                    signIn(PhoneAuthProvider.getCredential(response.verificationId, verificationSMSCode))
        }
    }

    private suspend fun signIn(credential: PhoneAuthCredential): AuthResult {
        return try {
            FirebaseAuth.getInstance().signInWithCredential(credential).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw LoginInteractor.InvalidVerificationCode()
        }
    }

    override suspend fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private sealed class VerificationCodeResponseImpl(
            override val isVerified: Boolean,
            override val phoneNumber: String) : PhoneVerificationModel {

        class OnVerificationCompleted(phoneNumber: String, val credential: PhoneAuthCredential) : VerificationCodeResponseImpl(isVerified = true, phoneNumber = phoneNumber) {

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

         class OnCodeSent(
                phoneNumber: String,
                val verificationId: String,
                val token: PhoneAuthProvider.ForceResendingToken?) : VerificationCodeResponseImpl(isVerified = false, phoneNumber = phoneNumber) {

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
                override fun createFromParcel(parcel: Parcel): OnCodeSent = OnCodeSent(parcel)
                override fun newArray(size: Int): Array<OnCodeSent?> = arrayOfNulls(size)

            }
        }
    }

}