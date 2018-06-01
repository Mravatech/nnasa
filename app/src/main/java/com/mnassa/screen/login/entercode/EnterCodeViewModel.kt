package com.mnassa.screen.login.entercode

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 23.02.2018.
 */
interface EnterCodeViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>
    var verificationResponse: PhoneVerificationModel

    fun resendCode()
    fun verifyCode(code: String)

    sealed class OpenScreenCommand {
        class MainScreen : OpenScreenCommand()
        class RegistrationScreen : OpenScreenCommand()
        class SelectAccount(val accounts: List<ShortAccountModel>) : OpenScreenCommand()
    }
}