package com.mnassa.domain.model

import java.util.*

/**
 * Created by Peter on 3/30/2018.
 */
interface TransactionModel : Model {
    val amount: Long
    val afterBalance: Long
    val time: Date
    val type: String
    val description: String?
    val byAccount: ShortAccountModel?
    val fromAccount: ShortAccountModel?
    val toAccount: ShortAccountModel?
}