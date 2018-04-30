package com.mnassa.screen.comments.rewarding

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_rewarding.view.*
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
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tilComplaintMessage.hint
            toolbar.actionButtonClickable = true
            toolbar.withActionButton(fromDictionary(R.string.rewarding_send), {
                close()
            })
//            etPoints
            tvName.text = accountModel?.formattedName
//            ["toAid": "-LAJ_dO12MFD0mJWYvqe", "amount": 2, "type": "rewardForComment", "fromAid": "-LAMUZAe1vrfyeN8HH2d", "commentId": "-LBKyV-9x51S5z8ruwsQ"]
            toolbar.title = fromDictionary(R.string.rewarding_title)
            tvRewardingInfo.text = fromDictionary(R.string.rewarding_info)
            tvPointInfo.text = fromDictionary(R.string.rewarding_for_comment)
//            etComplaintMessage
        }
    }

    companion object {
        private const val REWARDING_ACCOUNT = "REWARDING_ACCOUNT"
        private const val REWARDING_COMMENT = "REWARDING_COMMENT"
        fun newInstance(account: ShortAccountModel, commentId: String): RewardingController {
            val params = Bundle()
            params.putSerializable(REWARDING_ACCOUNT, account)
            params.putString(REWARDING_COMMENT, commentId)
            return RewardingController(params)
        }
    }
}