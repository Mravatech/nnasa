package com.mnassa.screen.splash

import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.splash.SplashViewModel.NextScreen.LOGIN
import com.mnassa.screen.splash.SplashViewModel.NextScreen.MAIN
import kotlinx.android.synthetic.main.controller_splash.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/20/2018.
 */
class SplashController : MnassaControllerImpl<SplashViewModel>(){

    override val layoutId: Int = R.layout.controller_splash
    override val viewModel: SplashViewModel by instance()
    private val appInfoProvider: AppInfoProvider by instance()



    override fun onViewCreated(view: View) {
        super.onViewCreated(view)



        view.tvApplicationName.text = String.format("%s %s", appInfoProvider.appName, appInfoProvider.versionName)

        launchCoroutineUI {
            viewModel.openNextScreenChannel.consumeEach {
                val controller = when (it) {
                    LOGIN -> EnterPhoneController.newInstance()
                    MAIN -> MainController.newInstance()
                }
                open(controller)
                return@launchCoroutineUI
            }
        }
    }

    override fun subscribeToSupportedApiStatus() = closeApiNotSupportedDialog()

    companion object {
        fun newInstance() = SplashController()
    }
}