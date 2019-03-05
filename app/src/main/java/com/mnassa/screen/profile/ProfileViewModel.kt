package com.mnassa.screen.profile

import com.mnassa.domain.model.*
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {
    val postsPagination: PaginationController

    val profileChannel: BroadcastChannel<ProfileAccountModel>
    val statusesConnectionsChannel: BroadcastChannel<ConnectionStatus>
    val postChannel: BroadcastChannel<ListItemEvent<List<PostModel>>>
    val interestsChannel: BroadcastChannel<List<TagModel>>
    val offersChannel: BroadcastChannel<List<TagModel>>
    val closeScreenChannel: BroadcastChannel<Unit>

    fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int)

    fun sendConnectionStatus(connectionStatus: ConnectionStatus, aid: String)

    fun sendComplaint(id: String, reason: String, authorText: String?)
    suspend fun retrieveComplaints(): List<TranslatedWordModel>

    fun inviteToGroup(group: GroupModel)
}