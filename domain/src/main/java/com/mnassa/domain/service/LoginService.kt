package com.mnassa.domain.service

import java.io.Serializable

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginService {
    suspend fun requestVerificationCode(phoneNumber: String): VerificationCodeResponse
    suspend fun login()

    interface VerificationCodeResponse: Serializable {
        val isVerificationNeeded: Boolean
    }
}