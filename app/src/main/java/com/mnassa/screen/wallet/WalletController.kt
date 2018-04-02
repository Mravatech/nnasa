package com.mnassa.screen.wallet

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.wallet.send.SendPointsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_wallet.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/29/2018.
 */
class WalletController : MnassaControllerImpl<WalletViewModel>(), SendPointsController.OnSentPointsResultListener {
    override val layoutId: Int = R.layout.controller_wallet
    override val viewModel: WalletViewModel by instance()
    private val transactionsAdapter = WalletRVAdapter()
    private val dialogHelper: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvBalanceLabel.text = fromDictionary(R.string.wallet_balance)
            tvTransactions.adapter = transactionsAdapter

            launchCoroutineUI {
                viewModel.currentBalanceChannel.consumeEach { tvBalance.text = it.toString() }
            }

            launchCoroutineUI {
                viewModel.spentPointsChannel.consumeEach { tvSpent.text = it.toString() }
            }

            launchCoroutineUI {
                viewModel.gainedPointsChannel.consumeEach { tvGained.text = it.toString() }
            }

            transactionsAdapter.isLoadingEnabled = true
            launchCoroutineUI {
                viewModel.transactionsChannel.consumeEach {
                    transactionsAdapter.isLoadingEnabled = false
                    transactionsAdapter.set(it)
                }
            }

            btnCreateTransaction.setOnClickListener {
                open(SendPointsController.newInstance(this@WalletController))
            }
        }
    }

    override fun onPointsSent(amount: Long, recipient: ShortAccountModel) {
        launchCoroutineUI {
            val view = getViewSuspend()

            val message = SpannableStringBuilder(fromDictionary(R.string.send_points_success_description))
            val spanStart = message.length
            message.append(" ")
            message.append(amount.toString())
            message.append(" ")
            message.append(fromDictionary(R.string.send_points_success_description_points))
            message.setSpan(StyleSpan(Typeface.BOLD), spanStart, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.append(" ")
            message.append(fromDictionary(R.string.send_points_success_description_to).format(recipient.formattedName))

            dialogHelper.showSuccessDialog(
                    view.context,
                    fromDictionary(R.string.send_points_success_title),
                    message)
        }
    }

    companion object {
        fun newInstance() = WalletController()
    }
}