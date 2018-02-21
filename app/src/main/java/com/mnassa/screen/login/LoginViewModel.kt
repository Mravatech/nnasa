package com.mnassa.screen.login

import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface LoginViewModel : MnassaViewModel {
    val openScreenChannel: ReceiveChannel<ScreenType>

    fun requestVerificationCode(phoneNumber: String)
    fun login(verificationCode: String)

    enum class ScreenType {
        MAIN
    }

}