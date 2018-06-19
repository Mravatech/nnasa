package com.mnassa.screen.main

import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/21/2018.
 */
interface MainViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<ScreenType>

    val unreadChatsCountChannel: BroadcastChannel<Int>
    val unreadNotificationsCountChannel: BroadcastChannel<Int>
    val unreadConnectionsCountChannel: BroadcastChannel<Int>
    val unreadEventsAndNeedsCountChannel: BroadcastChannel<Int>

    val currentAccountChannel: BroadcastChannel<ShortAccountModel>
    val availableAccountsChannel: BroadcastChannel<List<ShortAccountModel>>

    val showAddTagsDialog: BroadcastChannel<Unit>
    suspend fun getInterests(): List<TagModel>
    suspend fun getOffers(): List<TagModel>
    suspend fun getProfile(): ProfileAccountModel?

    fun selectAccount(account: ShortAccountModel)
    fun logout()
    fun resetAllNotifications()

    enum class ScreenType
}