package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.TransactionDbEntity
import com.mnassa.data.network.bean.firebase.TransactionMemberDbEntity
import com.mnassa.domain.interactor.DictionaryInteractor
import com.mnassa.domain.model.TransactionModel
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.domain.model.impl.TransactionModelImpl
import com.mnassa.domain.repository.DictionaryRepository
import java.util.*

/**
 * Created by Peter on 3/30/2018.
 */
class WalletConverter(private val lazyDictionaryInteractor: () -> DictionaryInteractor) : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertTransaction)
    }

    private fun convertTransaction(input: TransactionDbEntity, token: Any?, convertersContext: ConvertersContext): TransactionModel {
        return TransactionModelImpl(
                id = input.id,
                time = Date(input.transactionAt),
                type = fromDictionary("_transaction_" + input.type),
                afterBalance = input.afterBalance,
                amount = input.amount,
                byAccount = input.by?.run { convertUser(this, convertersContext) },
                fromAccount = input.from?.run { convertUser(this, convertersContext) },
                toAccount = input.to?.run { convertUser(this, convertersContext) },
                description = input.userDescription
        )
    }

    private fun convertUser(input: Map<String, TransactionMemberDbEntity>, converter: ConvertersContext): TransactionSideModel? {
        return input.entries.map { (userOrGroupId, userBody) ->
            TransactionSideModel(
                    id = userOrGroupId,
                    name = userBody.title ?: userBody.organizationName
                    ?: (userBody.firstName + " " + userBody.lastName),
                    isGroup = !userBody.title.isNullOrBlank(),
                    isAccount = userBody.title.isNullOrBlank()
            )
        }.firstOrNull()
    }

    private fun fromDictionary(key: String): String {
        val result: String by lazyDictionaryInteractor().getWord(key)
        return result.replace("%i", "%d")
    }
}