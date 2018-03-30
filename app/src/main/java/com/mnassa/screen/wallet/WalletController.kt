package com.mnassa.screen.wallet

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_wallet.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 3/29/2018.
 */
class WalletController : MnassaControllerImpl<WalletViewModel>() {
    override val layoutId: Int = R.layout.controller_wallet
    override val viewModel: WalletViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvBalanceLabel.text = fromDictionary(R.string.wallet_balance)

            launchCoroutineUI {
                viewModel.currentBalanceChannel.consumeEach {
                    tvBalance.text = it.toString()
                }
            }

            launchCoroutineUI {
                viewModel.spentPointsChannel.consumeEach {
                    tvSpent.text = it.toString()
                }
            }

            launchCoroutineUI {
                viewModel.gainedPointsChannel.consumeEach {
                    tvGained.text = it.toString()
                }
            }

            launchCoroutineUI {
                viewModel.transactionsChannel.consumeEach {
                    Timber.e(it.joinToString())
                }
            }
        }
    }

    companion object {
        fun newInstance() = WalletController()
    }
}