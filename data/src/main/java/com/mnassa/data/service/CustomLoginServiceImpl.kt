package com.mnassa.data.service

import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.extensions.await
import com.mnassa.data.network.api.CustomAuthApi
import com.mnassa.data.network.bean.retrofit.request.CheckSmsRequest
import com.mnassa.data.network.bean.retrofit.request.SendSmsRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.service.CustomLoginService
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/**
 * Created by Peter on 9/25/2018.
 */
class CustomLoginServiceImpl(
        private val authApi: CustomAuthApi,
        private val exceptionHandler: ExceptionHandler,
        private val firebaseLoginService: FirebaseLoginService,
        private val appInfoProvider: AppInfoProvider
) : CustomLoginService {

    override suspend fun requestVerificationCode(phoneNumber: String): ReceiveChannel<PhoneVerificationModel> {
        return GlobalScope.produce(Dispatchers.Unconfined) {
            val authId = authApi.sendSms(SendSmsRequest(phone = phoneNumber, isTest = appInfoProvider.isDebug || appInfoProvider.isCustomAuth)).handleException(exceptionHandler).data.id
            send(OnCodeSent(phoneNumber = phoneNumber, verificationId = authId, token = null))
        }
    }

    override suspend fun signIn(verificationSMSCode: String, response: PhoneVerificationModel) {
        response as OnCodeSent
        val token = authApi.checkSms(CheckSmsRequest(phone = response.phoneNumber, code = verificationSMSCode, id = response.verificationId)).handleException(exceptionHandler).data.token
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInWithCustomToken(token).await(exceptionHandler)
        }
    }

    override suspend fun signOut() {
        firebaseLoginService.signOut()
    }
}