package com.mnassa.screen.wallet.send

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.google.android.material.tabs.TabLayout
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.formattedName
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.select.SelectConnectionController
import com.mnassa.screen.group.select.SelectGroupController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_send_points.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/2/2018.
 */
class SendPointsController(args: Bundle) : MnassaControllerImpl<SendPointsViewModel>(args), SelectConnectionController.OnProfileSelectedListener, SelectGroupController.OnGroupSelectedListener {
    override val layoutId: Int = R.layout.controller_send_points
    private val sender = args[EXTRA_SENDER] as TransactionSideModel
    override val viewModel: SendPointsViewModel by instance()
    override var selectedAccount: ShortAccountModel? = null
        set(value) {
            if (field != value) {
                bindRecipient(value, null)
                field = value
            }
        }
    private var selectedGroup: GroupModel? = null
        set(value) {
            if (field != value) {
                bindRecipient(null, value)
                field = value
            }
        }

    private fun bindRecipient(user: ShortAccountModel?, group: GroupModel?) {
        if (user != null) selectedGroup = null
        if (group != null) selectedAccount = null

        launchCoroutineUI {
            getViewSuspend().etRecipient.setText(user?.formattedName ?: group?.formattedName)
            updateSendButtonState()
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.send_points_action)) {
                val recipient = selectedGroup?.let { TransactionSideModel(it) }
                        ?: selectedAccount?.let { TransactionSideModel(it) }
                        ?: return@withActionButton

                viewModel.sendPoints(
                        amount = etAmount.text.toString().toLong(),
                        sender = sender,
                        recipient = recipient,
                        description = etComment.text.toString().takeIf { it.isNotBlank() }
                )
            }

            etRecipient.setOnClickListener {
                when (tlRecipientType.selectedTabPosition) {
                    RECIPIENT_USER -> {
                        val controller = if (sender.isGroup) {
                            SelectConnectionController.newInstanceWithCommunityMembers(this@SendPointsController, communityId = sender.id)
                        } else {
                            SelectConnectionController.newInstance(this@SendPointsController)
                        }
                        open(controller)
                    }
                    RECIPIENT_GROUP -> open(SelectGroupController.newInstance(this@SendPointsController, onlyAdmin = false))
                }
            }
            etRecipient.hint = fromDictionary(R.string.send_points_recipient_hint)
            etComment.hint = fromDictionary(R.string.send_points_comments_hint)
            etAmount.addTextChangedListener(SimpleTextWatcher { updateSendButtonState() })

            tlRecipientType.newTab().let {
                it.text = fromDictionary(R.string.transaction_recipient_user)
                tlRecipientType.addTab(it)
                if (selectedAccount != null) it.select()
            }
            launchCoroutineUI {
                if (viewModel.hasAnyGroup()) {
                    tlRecipientType.newTab().let {
                        it.text = fromDictionary(R.string.transaction_recipient_group)
                        tlRecipientType.addTab(it)
                        if (selectedGroup != null) it.select()
                    }
                }
                bindRecipient(selectedAccount, selectedGroup)
            }

            tlRecipientType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                override fun onTabSelected(tab: TabLayout.Tab?) = bindRecipient(null, null)
            })

            bindRecipient(selectedAccount, selectedGroup)
            updateSendButtonState()
        }

        launchCoroutineUI {
            viewModel.resultListenerChannel.consumeEach {
                val target = targetController
                if (target is OnSentPointsResultListener) {
                    target.onPointsSent(
                            amount = it.amount,
                            sender = it.sender,
                            recipient = it.recipient,
                            description = it.description
                    )
                }
                close()
            }
        }
    }

    override fun onGroupSelected(group: GroupModel) {
        selectedGroup = group
    }

    private fun updateSendButtonState() {
        with(view ?: return) {
            toolbar.actionButtonClickable = (selectedAccount != null || selectedGroup != null) && !etAmount.text.isNullOrBlank()
        }
    }

    companion object {
        private const val EXTRA_SENDER = "EXTRA_SENDER"
        private const val RECIPIENT_USER = 0
        private const val RECIPIENT_GROUP = 1

        fun <T> newInstance(listener: T, sender: TransactionSideModel): SendPointsController where T : OnSentPointsResultListener, T : Controller {
            val args = Bundle()
            args.putSerializable(EXTRA_SENDER, sender)
            return SendPointsController(args).apply { targetController = listener }
        }
    }

    interface OnSentPointsResultListener {
        fun onPointsSent(amount: Long, sender: TransactionSideModel, recipient: TransactionSideModel, description: String?)
    }
}