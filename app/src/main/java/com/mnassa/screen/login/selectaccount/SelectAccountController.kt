package com.mnassa.screen.login.selectaccount

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_select_account.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountController : MnassaControllerImpl<SelectAccountViewModel>() {
    override val layoutId: Int = R.layout.controller_select_account
    override val viewModel: SelectAccountViewModel by instance()
    private val adapter = AccountsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.choose_profile_title)
            rvAccounts.layoutManager = LinearLayoutManager(view.context)
            rvAccounts.adapter = adapter

            adapter.onItemClickListener = { viewModel.selectAccount(it) }
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.accountsListChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
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
        fun newInstance() = SelectAccountController()
    }
}