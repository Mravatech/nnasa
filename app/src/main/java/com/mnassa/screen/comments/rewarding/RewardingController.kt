package com.mnassa.screen.comments.rewarding

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.RewardModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.domain.model.impl.RewardModelImpl
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_rewarding.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/30/2018
 */
class RewardingController(args: Bundle) : MnassaControllerImpl<RewardingViewModel>(args) {
    override val layoutId: Int = R.layout.controller_rewarding
    override val viewModel: RewardingViewModel by instance()
    private val accountModel: ShortAccountModel? by lazy { args.getSerializable(REWARDING_ACCOUNT) as ShortAccountModel? }
    private val commentId: String? by lazy { args.getString(REWARDING_COMMENT) }
    private val resultListener by lazy { targetController as RewardingResult }
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tilComment.hint = fromDictionary(R.string.rewarding_you_can_add_comment)
            toolbar.actionButtonClickable = true
            toolbar.withActionButton(fromDictionary(R.string.rewarding_send), {
                val points = etPoints.text.toString()
                if (points.isEmpty() || points == ZERO_POINTS) {
                    etPoints.error = "should not be empty or 0" //todo from dictionary
                    return@withActionButton
                }
                resultListener.onRewardApply = RewardModelImpl(
                        recipientId = requireNotNull(accountModel).id,
                        amount = points.toLong(),
                        commentId = requireNotNull(commentId),
                        userDescription = etComment.text.toString().takeIf { it.isNotBlank() }
                )
                close()
            })
            tvName.text = accountModel?.formattedName
//            ["toAid": "-LAJ_dO12MFD0mJWYvqe", "amount": 2, "type": "rewardForComment", "fromAid": "-LAMUZAe1vrfyeN8HH2d", "commentId": "-LBKyV-9x51S5z8ruwsQ", "userDescription" : "text"]
            toolbar.title = fromDictionary(R.string.rewarding_title)
            tvRewardingInfo.text = fromDictionary(R.string.rewarding_info)
            tvPointInfo.text = fromDictionary(R.string.rewarding_for_comment)
        }
        launchCoroutineUI {
            viewModel.defaultRewardCountChannel.consumeEach {
                view.etPoints.setText(it.toString())
            }
        }
    }

    interface RewardingResult {
        var onRewardApply: RewardModel?
    }

    companion object {
        private const val REWARDING_ACCOUNT = "REWARDING_ACCOUNT"
        private const val REWARDING_COMMENT = "REWARDING_COMMENT"
        private const val ZERO_POINTS = "0"
        fun newInstance(account: ShortAccountModel, commentId: String): RewardingController {
            val params = Bundle()
            params.putSerializable(REWARDING_ACCOUNT, account)
            params.putString(REWARDING_COMMENT, commentId)
            return RewardingController(params)
        }
    }
}