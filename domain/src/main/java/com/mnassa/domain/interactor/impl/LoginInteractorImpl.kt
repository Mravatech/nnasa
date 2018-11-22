package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.launchUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.core.events.impl.SimpleCompositeEventListener
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.UserStatusModel
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.CustomLoginService
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository,
                          private val userProfileInteractor: UserProfileInteractor,
                          private val postsRepository: PostsRepository,
                          private val loginService: FirebaseLoginService,
                          private val customLoginService: CustomLoginService) : LoginInteractor {

    private var useCustomAuth = false

    override val onLogoutListener: SimpleCompositeEventListener<LogoutReason> = SimpleCompositeEventListener()

    override fun isLoggedIn(): Boolean = userRepository.getAccountIdOrNull() != null

    override suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel?,
            promoCode: String?
    ): ReceiveChannel<PhoneVerificationModel> {

        useCustomAuth = loginService.checkPhone(phoneNumber, promoCode).useCustomAuth
        return if (useCustomAuth) {
            customLoginService.requestVerificationCode(phoneNumber)
        } else loginService.requestVerificationCode("+$phoneNumber", previousResponse)
    }

    override suspend fun processLoginByEmail(email: String, password: String): PhoneVerificationModel {
        return loginService.processLoginByEmail(email, password)
    }

    override suspend fun signIn(response: PhoneVerificationModel, verificationSMSCode: String?): List<ShortAccountModel> {
        if (useCustomAuth) {
            customLoginService.signIn(verificationSMSCode ?: "invalid code", response)
        } else {
            loginService.signIn(verificationSMSCode, response)
        }
        return userRepository.getAccounts()
    }

    override suspend fun signOut(reason: LogoutReason) {
        val wasLoggedIn = isLoggedIn()
        if (useCustomAuth) {
            customLoginService.signOut()
        } else {
            loginService.signOut()
        }
        userRepository.setCurrentAccount(null)
        postsRepository.clearSavedPosts()
        if (wasLoggedIn) {
            launchUI { onLogoutListener.emit(reason) }
        }
    }

    override suspend fun handleUserStatus() {
        try {
            val firebaseId = userRepository.getFirebaseUserId()
            if (firebaseId == null) {
                userProfileInteractor.onAccountChangedListener.awaitFirst()
                handleUserStatus()
                return
            }

            var logoutJob: Job? = null

            userRepository.getUserStatusChannel(firebaseId).consumeEach {
                Timber.i("#USER_STATUS#: $firebaseId : $it")
                logoutJob?.cancel()
                if (it is UserStatusModel.Disabled) {
                    logoutJob = async(UI) {
                        delay(1_000)
                        signOut(LogoutReason.UserBlocked())
                    }
                    return@consumeEach
                }
            }
        } catch (e: Exception) {
            //do nothing
            Timber.e(e)
        }
        return handleUserStatus()
    }

    override suspend fun handleAccountStatus() {
        val listenAccountStatusJob: Job = async(UI) {
            val currentAccountId = userProfileInteractor.getAccountIdOrNull() ?: return@async
            var logoutJob: Job? = null
            userRepository.getAccountStatusChannel(currentAccountId).consumeEach {
                Timber.i("#ACCOUNT_STATUS#: $currentAccountId : $it")
                logoutJob?.cancel()
                if (it is UserStatusModel.Disabled) {
                    logoutJob = async(UI) {
                        delay(1_000)
                        signOut(LogoutReason.AccountBlocked())
                    }
                }
            }
        }
        userProfileInteractor.onAccountChangedListener.awaitFirst()
        listenAccountStatusJob.cancel()
        handleAccountStatus()
    }
}