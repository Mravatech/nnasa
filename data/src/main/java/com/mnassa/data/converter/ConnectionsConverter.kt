package com.mnassa.data.converter

import com.google.firebase.database.DataSnapshot
import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.extensions.mapList
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.RecommendedConnectionsImpl

/**
 * Created by Peter on 11.03.2018.
 */
class ConnectionsConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::recommendedConnection)
        convertersContext.registerConverter(this::statusesConnection)
    }

    private fun statusesConnection(statuses: String): ConnectionStatus {
        return when (statuses) {
            NetworkContract.ConnectionsStatus.CONNECTED -> ConnectionStatus.CONNECTED
            NetworkContract.ConnectionsStatus.REQUESTED -> ConnectionStatus.REQUESTED
            NetworkContract.ConnectionsStatus.SENT -> ConnectionStatus.SENT
            NetworkContract.ConnectionsStatus.DISCONNECTED -> ConnectionStatus.DISCONNECTED
            NetworkContract.ConnectionsStatus.RECOMMENDED -> ConnectionStatus.RECOMMENDED
            else -> ConnectionStatus.NONE
        }
    }

    private fun recommendedConnection(dataSnapshot: DataSnapshot, token: Any?, converter: ConvertersContext): RecommendedConnectionsImpl {
        val result = mutableMapOf<String, MutableList<ShortAccountModel>>()

        dataSnapshot.children.forEach {
            if (it != null) {
                val group = result.getOrPut(it.key ?: "") { mutableListOf() }
                it.children.forEach {
                    group.addAll(it.mapList<ShortAccountDbEntity>().map { converter.convert(it, ShortAccountModel::class.java) })
                }
            }
        }

        return RecommendedConnectionsImpl(recommendations = result)
    }
}