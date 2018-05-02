package com.mnassa.domain.model

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
interface RewardModel {
    var recipientId: String
    var amount: Long
    var commentId: String
    var userDescription: String?
}