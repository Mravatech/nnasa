package com.mnassa.domain.interactor.impl

import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.launchUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.UserStatusModel
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.domain.repository.UserRepository
import com.mnassa.domain.service.CustomLoginService
import com.mnassa.domain.service.FirebaseLoginService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filterNotNull
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class LoginInteractorImpl(private val userRepository: UserRepository,
                          private val userProfileInteractor: UserProfileInteractor,
                          private val postsRepository: PostsRepository,
                          private val loginService: FirebaseLoginService,
                          private val appInfoProvider: AppInfoProvider,
                          private val customLoginService: CustomLoginService) : LoginInteractor {

    private var useCustomAuth = false

    override val onLogoutListener: BroadcastChannel<LogoutReason> = BroadcastChannel(1)

    override fun isLoggedIn(): Boolean = userRepository.getAccountIdOrNull() != null

    override suspend fun requestVerificationCode(
            phoneNumber: String,
            previousResponse: PhoneVerificationModel?,
            promoCode: String?
    ): ReceiveChannel<PhoneVerificationModel> {

        useCustomAuth = loginService.checkPhone(phoneNumber, promoCode).useCustomAuth || appInfoProvider.isCustomAuth
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
        if (wasLoggedIn) {
            GlobalScope.launchUI { onLogoutListener.send(reason) }
        }
    }

    override fun CoroutineScope.handleUserStatus() {
        var logoutJob: Job? = null
        var consumerJob: Job? = null
        handle(userRepository::getFirebaseUserId) { firebaseUserId ->
            logoutJob?.cancel()
            consumerJob?.cancel()
            consumerJob = launchWorker {

                // Listen the channel of user
                // status.
                userRepository.getUserStatusChannel(firebaseUserId).consumeEach {
                    Timber.i("#USER_STATUS#: $firebaseUserId : $it")

                    // Log out after small delay after getting
                    // status change.
                    logoutJob?.cancel()
                    logoutJob = if (it is UserStatusModel.Disabled) {
                        GlobalScope.asyncUI {
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

    override fun CoroutineScope.handleAccountStatus() {
        var logoutJob: Job? = null
        var consumerJob: Job? = null
        handle(userProfileInteractor::getAccountIdOrNull) { accountId ->
            logoutJob?.cancel()
            consumerJob?.cancel()
            consumerJob = launchWorker {

                // Listen the channel of account
                // status.
                userRepository.getAccountStatusChannel(accountId).consumeEach {
                    Timber.i("#ACCOUNT_STATUS#: $accountId : $it")

                    // Log out after small delay after getting
                    // status change.
                    logoutJob?.cancel()
                    logoutJob = if (it is UserStatusModel.Disabled) {
                        GlobalScope.asyncUI {
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

    override fun CoroutineScope.handleAccountRefresh() {
        launchWorker {
            var consumerJob: Job? = null
            var prevAccountId: String? = null
            while (isActive) {
                val accountId = userProfileInteractor.getAccountIdOrNull()
                if (prevAccountId != accountId) {
                    prevAccountId = accountId

                    // Subscribe to account's model, mostly to get the
                    // serial number.
                    consumerJob?.cancel()
                    consumerJob = if (accountId != null) {
                        launch(Dispatchers.Default) {
                            userProfileInteractor.getAccountByIdChannel(accountId)
                                .filterNotNull()
                                .consumeEach {
                                    val curAccountId = userProfileInteractor.getAccountIdOrNull()
                                    if (curAccountId == accountId) {
                                        userProfileInteractor.setCurrentUserAccount(it)
                                    }
                                }
                        }
                    } else {
                        null
                    }
                }

                userProfileInteractor.onAccountChangedListener.awaitFirst()
            }
        }
    }

    private fun <T : Any> CoroutineScope.handle(
        getter: () -> T?,
        callback: suspend CoroutineScope.(T) -> Unit
    ): Job {
        return launchWorker {
            while (isActive) {
                do {
                    val model = getter()
                    if (model != null) {
                        callback(model)
                    }
                } while (model != getter())

                userProfileInteractor.onAccountChangedListener.awaitFirst()
            }
        }
    }
}