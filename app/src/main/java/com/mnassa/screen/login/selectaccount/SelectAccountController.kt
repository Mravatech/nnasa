package com.mnassa.screen.login.selectaccount

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_select_account.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountController : MnassaControllerImpl<SelectAccountViewModel>() {
    override val layoutId: Int = R.layout.controller_select_account
    override val viewModel: SelectAccountViewModel by instance()
    private val adapter = AccountsRecyclerViewAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        adapter.onItemClickListener = { viewModel.selectAccount(it) }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.choose_profile_title)
            rvAccounts.layoutManager = LinearLayoutManager(view.context)
            rvAccounts.adapter = adapter
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
                    is SelectAccountViewModel.OpenScreenCommand.MainScreen -> open(MainController.newInstance())
                }
            }
        }
    }

    override fun onDestroyView(view: View) {
        view.rvAccounts.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = SelectAccountController()
    }
}