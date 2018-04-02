package com.mnassa.screen.wallet.send

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.select.SelectConnectionController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_send_points.view.*

/**
 * Created by Peter on 4/2/2018.
 */
class SendPointsController : MnassaControllerImpl<SendPointsViewModel>(), SelectConnectionController.OnProfileSelectedListener {
    override val layoutId: Int = R.layout.controller_send_points
    override val viewModel: SendPointsViewModel by instance()
    override var selectedAccount: ShortAccountModel? = null
        set(value) {
            field = value
            bindSelectedAccount(value)
        }

    private fun bindSelectedAccount(value: ShortAccountModel?) {
        launchCoroutineUI {
            getViewSuspend().etRecipient.setText(value?.formattedName)
            updateSendButtonState()
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.send_points_action)) {
                viewModel.sendPoints(
                        amount = etAmount.text.toString().toLong(),
                        recipient = requireNotNull(selectedAccount),
                        description = etComment.text.toString().takeIf { it.isNotBlank() }
                )
            }

            etRecipient.setOnClickListener {
                val controller = SelectConnectionController.newInstance()
                controller.targetController = this@SendPointsController
                open(controller)
            }
            etRecipient.hint = fromDictionary(R.string.send_points_recipient_hint)
            etComment.hint = fromDictionary(R.string.send_points_comments_hint)
            etAmount.addTextChangedListener(SimpleTextWatcher { updateSendButtonState() })

            bindSelectedAccount(selectedAccount)
            updateSendButtonState()
        }
    }

    private fun updateSendButtonState() {
        with(view ?: return) {
            toolbar.actionButtonClickable = selectedAccount != null && etAmount.text.isNotBlank()
        }
    }

    companion object {
        fun newInstance() = SendPointsController()
    }
}