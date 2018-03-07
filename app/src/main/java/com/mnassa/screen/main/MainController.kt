package com.mnassa.screen.main

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class MainController : MnassaControllerImpl<MainViewModel>() {
    override val layoutId: Int = R.layout.controller_main
    override val viewModel: MainViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.btnLogout.setOnClickListener {
            viewModel.logout()
        }
        view.btnCrop.setOnClickListener {
            router.replaceTopController(RouterTransaction.with(ProfileController.newInstance()))
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    MainViewModel.ScreenType.LOGIN -> EnterPhoneController.newInstance()
                }

                router.replaceTopController(RouterTransaction.with(controller))
            }
        }

        launchCoroutineUI {
            viewModel.userName.consumeEach {
                view.tvUserName.text = "Hi, $it"
            }
        }
    }

    companion object {
        fun newInstance() = MainController()
    }
}