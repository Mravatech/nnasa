package com.mnassa.screen.splash

import android.view.View
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.LoginController
import com.mnassa.screen.main.MainController
import kotlinx.android.synthetic.main.controller_splash.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class SplashController : MnassaControllerImpl<SplashViewModel>() {
    override val layoutId: Int = R.layout.controller_splash
    override val viewModel: SplashViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        Timber.e("TEST Controller onViewCreated")

        val job = launchCoroutineUI {
            viewModel.countDown.consumeEach {
                view.tvTimer.text = it.toString()
                if (it == 0) {
                    openNextScreen()
                }
            }
        }
        job.invokeOnCompletion {
            Timber.e("TEST Controller invokeOnCompletion")
        }

        launchCoroutineUI {
            delay(3_000L)
            viewModel.message.consumeEach {
                Timber.e("TESTTTTT: consuemed $it")
                Toast.makeText(view.context, it, Toast.LENGTH_SHORT).show()
                delay(2_000L)
            }
        }
    }

    private suspend fun openNextScreen() {
        val nextScreen = when {
            viewModel.isLoggedIn() -> MainController.newInstance()
            else -> LoginController.newInstance()
        }

        router.replaceTopController(RouterTransaction.with(nextScreen))
    }

    companion object {
        fun newInstance() = SplashController()
    }

    init {
        Timber.e("TEST Controller constructor")
    }
}