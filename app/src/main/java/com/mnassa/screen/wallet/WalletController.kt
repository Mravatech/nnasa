package com.mnassa.screen.wallet

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.wallet.send.SendPointsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_wallet.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/29/2018.
 */
class WalletController(args: Bundle) : MnassaControllerImpl<WalletViewModel>(args), SendPointsController.OnSentPointsResultListener {
    override val layoutId: Int = R.layout.controller_wallet
    override val viewModel: WalletViewModel by instance(arg = getParams(args))
    private val languageProvider: LanguageProvider by instance()
    private val transactionsAdapter by lazy { WalletRVAdapter(languageProvider) }
    private val dialogHelper: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvTransactions.adapter = transactionsAdapter

            btnCreateTransaction.setOnClickListener {
                viewModel.createTransaction()
            }
        }

        launchCoroutineUI {
            viewModel.screenTitleChannel.consumeEach { view.toolbar.title = it }
        }

        launchCoroutineUI {
            viewModel.currentBalanceChannel.consumeEach { view.tvBalance.text = it.toString() }
        }

        launchCoroutineUI {
            viewModel.spentPointsChannel.consumeEach { view.tvSpent.text = it.toString() }
        }

        launchCoroutineUI {
            viewModel.gainedPointsChannel.consumeEach { view.tvGained.text = it.toString() }
        }

        launchCoroutineUI {
            viewModel.createTransaction.consumeEach {
                open(SendPointsController.newInstance(this@WalletController, it))
            }
        }

        transactionsAdapter.isLoadingEnabled = true
        val adapterRef = transactionsAdapter.asReference()
        launchCoroutineUI {
            viewModel.transactionsChannel.consumeEach {
                adapterRef().isLoadingEnabled = false
                adapterRef().set(it)
            }
        }
    }

    override fun onDestroyView(view: View) {
        view.rvTransactions.adapter = null
        super.onDestroyView(view)
    }

    override fun onPointsSent(amount: Long, sender: TransactionSideModel, recipient: TransactionSideModel, description: String?) =
            onPointsSent(amount, recipient.formattedName)

    private fun onPointsSent(amount: Long, recipient: String) {
        launchCoroutineUI {
            val message = SpannableStringBuilder(fromDictionary(R.string.send_points_success_description))
            val spanStart = message.length
            message.append(" ")
            message.append(amount.toString())
            message.append(" ")
            message.append(fromDictionary(R.string.send_points_success_description_points))
            message.setSpan(StyleSpan(Typeface.BOLD), spanStart, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.append(" ")
            message.append(fromDictionary(R.string.send_points_success_description_to).format(recipient))

            dialogHelper.showSuccessDialog(
                    getViewSuspend().context,
                    fromDictionary(R.string.send_points_success_title),
                    message)
        }
    }

    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance() = WalletController(Bundle())

        fun newInstanceGroup(group: GroupModel): WalletController {
            val args = Bundle()
            args.putSerializable(EXTRA_GROUP, group)
            return WalletController(args)
        }

        fun getParams(args: Bundle): WalletViewModel.WalletSource {
            val group = args[EXTRA_GROUP] as GroupModel?
            return group?.let { WalletViewModel.WalletSource.Group(it) }
                    ?: WalletViewModel.WalletSource.User()
        }
    }
}