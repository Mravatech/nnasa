package com.mnassa.domain.model.impl

import com.mnassa.domain.model.RewardModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
data class RewardModelImpl(
        override var recipientId: String,
        override var amount: Long,
        override var commentId: String,
        override var parentCommentId: String?,
        override var userDescription: String?
) : RewardModel