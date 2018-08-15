package com.mnassa.screen.main

import com.mnassa.domain.model.ShortAccountModel
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

    val groupsCountChannel: BroadcastChannel<Int>

    val currentAccountChannel: BroadcastChannel<ShortAccountModel>
    val availableAccountsChannel: BroadcastChannel<List<ShortAccountModel>>

    fun selectAccount(account: ShortAccountModel)
    fun logout()
    fun resetAllNotifications()

    enum class ScreenType
}