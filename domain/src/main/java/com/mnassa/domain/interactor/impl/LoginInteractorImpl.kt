package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.launchUI
import com.mnassa.core.addons.launchWorker
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
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
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

    override fun handleUserStatus(): Job {
        var logoutJob: Job? = null
        var consumerJob: Job? = null
        return handle(userRepository::getFirebaseUserId) { firebaseUserId ->
            logoutJob?.cancel()
            consumerJob?.cancel()
            consumerJob = launch(coroutineContext + DefaultDispatcher) {

                // Listen the channel of user
                // status.
                userRepository.getUserStatusChannel(firebaseUserId).consumeEach {
                    Timber.i("#USER_STATUS#: $firebaseUserId : $it")

                    // Log out after small delay after getting
                    // status change.
                    logoutJob?.cancel()
                    logoutJob = if (it is UserStatusModel.Disabled) {
                        async(UI) {
                            delay(1_000)
                            signOut(LogoutReason.UserBlocked())
                        }
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun handleAccountStatus(): Job {
        var logoutJob: Job? = null
        var consumerJob: Job? = null
        return handle(userProfileInteractor::getAccountIdOrNull) { accountId ->
            logoutJob?.cancel()
            consumerJob?.cancel()
            consumerJob = launch(coroutineContext + DefaultDispatcher) {

                // Listen the channel of account
                // status.
                userRepository.getAccountStatusChannel(accountId).consumeEach {
                    Timber.i("#ACCOUNT_STATUS#: $accountId : $it")

                    // Log out after small delay after getting
                    // status change.
                    logoutJob?.cancel()
                    logoutJob = if (it is UserStatusModel.Disabled) {
                        async(UI) {
                            delay(1_000)
                            signOut(LogoutReason.AccountBlocked())
                        }
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun <T : Any> handle(
        getter: () -> T?,
        callback: suspend CoroutineScope.(T) -> Unit
    ): Job {
        return launchWorker {
            while (isActive) {
                getter()
                    ?.let {
                        callback(it)
                    }

                userProfileInteractor.onAccountChangedListener.awaitFirst()
            }
        }
    }
}