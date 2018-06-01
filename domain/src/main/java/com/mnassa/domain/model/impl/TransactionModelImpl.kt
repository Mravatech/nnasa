package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TransactionModel
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
        override val byAccount: ShortAccountModel?,
        override val fromAccount: ShortAccountModel?,
        override val toAccount: ShortAccountModel?,
        override val description: String?
) : TransactionModel {
}