package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
interface RewardModel : Serializable {
    var recipientId: String
    var amount: Long
    var commentId: String
    var parentCommentId: String?
    var userDescription: String?
}