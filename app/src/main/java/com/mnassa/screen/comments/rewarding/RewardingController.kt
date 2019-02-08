package com.mnassa.screen.comments.rewarding

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.RewardModelImpl
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_send_points.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/30/2018
 */
class RewardingController(args: Bundle) : MnassaControllerImpl<RewardingViewModel>(args) {
    override val layoutId: Int = R.layout.controller_send_points
    override val viewModel: RewardingViewModel by instance()
    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_REWARDING_ACCOUNT) as ShortAccountModel }
    private val commentId: String by lazy { args.getString(EXTRA_REWARDING_COMMENT) }
    private val parentCommentId: String by lazy { args.getString(EXTRA_REWARDING_COMMENT_PARENT) }
    private val resultListener by lazy { targetController as RewardingResult }
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.rewarding_send)) {
                val points = etAmount.text.toString()
                resultListener.onApplyReward(RewardModelImpl(
                        recipientId = accountModel.id,
                        amount = points.toLong(),
                        commentId = commentId,
                        parentCommentId = parentCommentId,
                        userDescription = etComment.text.toString().takeIf { it.isNotBlank() }
                ))
                close()
            }
            toolbar.title = fromDictionary(R.string.rewarding_title)
            etRecipient.setText(accountModel.formattedName)
            etComment.hint = fromDictionary(R.string.rewarding_you_can_add_comment)
            etAmount.addTextChangedListener(SimpleTextWatcher {
                if (it.startsWith(ZERO_POINTS)) etAmount.text = null
                toolbar.actionButtonEnabled = etAmount.text.toString().isNotEmpty()
            })
        }
        launchCoroutineUI {
            viewModel.defaultRewardChannel.consumeEach {
                view.etAmount.setText(it.toString())
            }
        }
    }

    interface RewardingResult {
        fun onApplyReward(rewardModel: RewardModel)
    }

    companion object {
        private const val EXTRA_REWARDING_ACCOUNT = "EXTRA_REWARDING_ACCOUNT"
        private const val EXTRA_REWARDING_COMMENT = "EXTRA_REWARDING_COMMENT"
        private const val EXTRA_REWARDING_COMMENT_PARENT = "EXTRA_REWARDING_COMMENT_PARENT"
        private const val ZERO_POINTS = "0"
        fun <T> newInstance(listener: T, account: ShortAccountModel, comment: CommentModel): RewardingController where T : RewardingResult, T : Controller {
            val params = Bundle()
            params.putSerializable(EXTRA_REWARDING_ACCOUNT, account)
            params.putString(EXTRA_REWARDING_COMMENT, comment.id)
            params.putString(EXTRA_REWARDING_COMMENT_PARENT, comment.parentCommentId)

            val controller = RewardingController(params)
            controller.targetController = listener
            return controller
        }
    }
}