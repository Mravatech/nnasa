package com.mnassa.screen.login.selectaccount

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.AccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountController(params: Bundle) : MnassaControllerImpl<SelectAccountViewModel>(params) {
    override val layoutId: Int = R.layout.controller_select_account
    override val viewModel: SelectAccountViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        launchCoroutineUI {
            viewModel.showMessageChannel.consumeEach {
                Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val EXTRA_ACCOUNTS_LIST = "EXTRA_ACCOUNTS_LIST"

        fun newInstance(accounts: List<AccountModel>): SelectAccountController {
            require(accounts.size > 1) {
                "Accounts list must contain at least 2 models!"
            }

            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNTS_LIST, ArrayList(accounts))

            return SelectAccountController(params)
        }
    }
}