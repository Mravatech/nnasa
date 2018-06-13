package com.mnassa.screen.posts.offer.details.buy

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.domain.model.formattedName
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_send_points.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/2/2018.
 */
class BuyOfferController(args: Bundle) : MnassaControllerImpl<BuyOfferViewModel>(args) {
    override val layoutId: Int = R.layout.controller_send_points
    override val viewModel: BuyOfferViewModel by instance()
    private val recipient by lazy { args[EXTRA_RECIPIENT] as ShortAccountModel }
    private val price by lazy { args.getDouble(EXTRA_PRICE) }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.title = fromDictionary(R.string.offer_purchase_title)
            toolbar.withActionButton(fromDictionary(R.string.send_points_action)) {
                viewModel.sendPoints(
                        amount = price.toLong(),
                        recipient = recipient,
                        description = etComment.text.toString().takeIf { it.isNotBlank() }
                )
            }
            tvDescription.text = fromDictionary(R.string.offer_purchase_description)

            etRecipient.setText(recipient.formattedName)
            etRecipient.hint = fromDictionary(R.string.send_points_recipient_hint)
            etComment.hint = fromDictionary(R.string.send_points_comments_hint)
            etAmount.setText(price.toLong().toString())
            etAmount.isFocusable = false
            etAmount.isClickable = false

        }

        launchCoroutineUI {
            viewModel.resultListenerChannel.consumeEach {
                val target = targetController
                if (target is OnSentPointsResultListener) {
                    target.onPointsSent(it.amount, it.recipient)
                }
                close()
            }
        }
    }

    interface OnSentPointsResultListener {
        fun onPointsSent(amount: Long, recipient: TransactionSideModel)
    }

    companion object {
        private const val EXTRA_RECIPIENT = "EXTRA_RECIPIENT"
        private const val EXTRA_PRICE = "EXTRA_PRICE"

        fun newInstance(recipient: ShortAccountModel, price: Double): BuyOfferController {
            val args = Bundle()
            args.putSerializable(EXTRA_RECIPIENT, recipient)
            args.putDouble(EXTRA_PRICE, price)

            return BuyOfferController(args)
        }
    }
}