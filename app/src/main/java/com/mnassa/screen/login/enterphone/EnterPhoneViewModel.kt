package com.mnassa.screen.login.enterphone

import com.mnassa.domain.service.LoginService
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface EnterPhoneViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>
    val errorMessageChannel: BroadcastChannel<String>

    fun requestVerificationCode(phoneNumber: String)

    sealed class OpenScreenCommand {
        class EnterVerificationCode(val param: LoginService.VerificationCodeResponse): OpenScreenCommand()
        class MainScreen: OpenScreenCommand()
    }
}