package com.mnassa.screen.login.selectaccount

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import kotlinx.android.synthetic.main.controller_select_account.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountController(params: Bundle) : MnassaControllerImpl<SelectAccountViewModel>(params) {
    override val layoutId: Int = R.layout.controller_select_account
    override val viewModel: SelectAccountViewModel by instance()

    @Suppress("UNCHECKED_CAST")
    private val accounts by lazy { args.getSerializable(EXTRA_ACCOUNTS_LIST) as List<ShortAccountModel> }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvAccounts.layoutManager = LinearLayoutManager(view.context)
            val adapter = AccountsRecyclerViewAdapter(accounts.toMutableList())
            rvAccounts.adapter = adapter

            adapter.onItemClickListener = { viewModel.selectAccount(it) }
        }

        launchCoroutineUI {
            viewModel.showMessageChannel.consumeEach {
                Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
            }
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    is SelectAccountViewModel.OpenScreenCommand.MainScreen -> {
                        router.popToRoot()
                        router.replaceTopController(RouterTransaction.with(MainController.newInstance()))
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_ACCOUNTS_LIST = "EXTRA_ACCOUNTS_LIST"

        fun newInstance(accounts: List<ShortAccountModel>): SelectAccountController {
            require(accounts.size > 1) {
                "Accounts list must contain at least 2 models!"
            }
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNTS_LIST, ArrayList(accounts))
            return SelectAccountController(params)
        }
    }
}