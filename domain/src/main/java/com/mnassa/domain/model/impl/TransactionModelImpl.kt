package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import java.util.*

/**
 * Created by Peter on 3/30/2018.
 */
data class TransactionModelImpl(
        override var id: String,
        override val time: Date,
        override val type: String,
        override val amount: Long,
        override val afterBalance: Long,
        override val byAccount: TransactionSideModel?,
        override val fromAccount: TransactionSideModel?,
        override val toAccount: TransactionSideModel?,
        override val description: String?
) : TransactionModel {
}