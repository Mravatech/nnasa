package com.mnassa.domain.interactor.impl

import com.mnassa.core.events.impl.SimpleCompositeEventListener
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository, private val loginService: FirebaseLoginService) : LoginInteractor {
    override val onLogoutListener: SimpleCompositeEventListener<Unit> = SimpleCompositeEventListener()

    override suspend fun isLoggedIn(): Boolean {
        return userRepository.getCurrentAccount() != null
    }

    override suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel?,
            promoCode: String?
    ): ReceiveChannel<PhoneVerificationModel> {

        loginService.checkPhone(phoneNumber, promoCode)
        return loginService.requestVerificationCode("+$phoneNumber", previousResponse)
    }

    override suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel {
        return loginService.processLoginByEmail(email, password)
    }

    override suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String?): List<ShortAccountModel> {
        loginService.signIn(verificationSMSCode, response)
        return userRepository.getAccounts()
    }

    override suspend fun signOut() {
        loginService.signOut()
        userRepository.setCurrentAccount(null)

        launch(UI) { onLogoutListener.emit(Unit) }
    }
}