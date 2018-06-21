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
    val byAccount: TransactionSideModel?
    val fromAccount: TransactionSideModel?
    val toAccount: TransactionSideModel?
}

data class TransactionSideModel(
        override var id: String,
        private val name: String,
        val isAccount: Boolean,
        val isGroup: Boolean) : Model {
    val formattedName: String get() = name

    constructor(account: ShortAccountModel) : this(account.id, account.formattedName, true, false)
    constructor(group: GroupModel) : this(group.id, group.name, false, true)

    init {
        assert(isAccount != isGroup)
    }
}