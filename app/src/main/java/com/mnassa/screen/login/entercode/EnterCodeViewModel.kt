package com.mnassa.screen.login.entercode

import com.mnassa.domain.service.LoginService
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 23.02.2018.
 */
interface EnterCodeViewModel : MnassaViewModel {
    val openScreenChannel: ReceiveChannel<OpenScreenCommand>
    val showMessageChannel: ReceiveChannel<String>
    var verificationResponse: LoginService.VerificationCodeResponse

    fun resendCode()
    fun verifyCode(code: String)

    sealed class OpenScreenCommand {
        class MainScreen : OpenScreenCommand()
    }
}