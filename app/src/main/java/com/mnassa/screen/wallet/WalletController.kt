package com.mnassa.screen.wallet

import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/29/2018.
 */
class WalletController : MnassaControllerImpl<WalletViewModel>() {
    override val layoutId: Int = R.layout.controller_wallet
    override val viewModel: WalletViewModel by instance()

    companion object {
        fun newInstance() = WalletController()
    }
}