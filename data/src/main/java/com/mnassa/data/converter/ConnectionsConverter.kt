package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.database.DataSnapshot
import com.mnassa.data.extensions.mapList
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.data.repository.DatabaseContract
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.RecommendedConnectionsImpl

/**
 * Created by Peter on 11.03.2018.
 */
class ConnectionsConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::recommendedConnection)
    }

    private fun recommendedConnection(dataSnapshot: DataSnapshot, token: Any?, converter: ConvertersContext): RecommendedConnectionsImpl {
        val byPhone = mutableMapOf<String, List<ShortAccountModel>>()
        val byGroup = mutableMapOf<String, List<ShortAccountModel>>()
        val byEvent = mutableMapOf<String, List<ShortAccountModel>>()

        dataSnapshot.child(DatabaseContract.TABLE_CONNECTIONS_RECOMMENDED_COL_BY_PHONE)?.children?.forEach {
            byPhone[it.key] = it.mapList<ShortAccountDbEntity>().map { converter.convert(it, ShortAccountModel::class.java) }
        }

        dataSnapshot.child(DatabaseContract.TABLE_CONNECTIONS_RECOMMENDED_COL_BY_GROUPS)?.children?.forEach {
            byGroup[it.key] = it.mapList<ShortAccountDbEntity>().map { converter.convert(it, ShortAccountModel::class.java) }
        }

        dataSnapshot.child(DatabaseContract.TABLE_CONNECTIONS_RECOMMENDED_COL_BY_EVENTS)?.children?.forEach {
            byEvent[it.key] = it.mapList<ShortAccountDbEntity>().map { converter.convert(it, ShortAccountModel::class.java) }
        }

        return RecommendedConnectionsImpl(byEvents = byEvent, byGroups = byGroup, byPhone = byPhone)
    }
}