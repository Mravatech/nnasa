package com.mnassa.screen.profile

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(
        private val accountId: String,
        private val tagInteractor: TagInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val connectionsInteractor: ConnectionsInteractor,
        private val postsInteractor: PostsInteractor,
        private val complaintInteractor: ComplaintInteractor,
        private val groupsInteractor: GroupsInteractor
) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: ConflatedBroadcastChannel<ProfileAccountModel> = ConflatedBroadcastChannel()
    override val statusesConnectionsChannel: ConflatedBroadcastChannel<ConnectionStatus> = ConflatedBroadcastChannel()
    override val postChannel: ConflatedBroadcastChannel<ListItemEvent<List<PostModel>>> = ConflatedBroadcastChannel()
    override val interestsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val offersChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)
    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            reportsList = complaintInteractor.getReports()
        }

        handleException {
            userProfileInteractor.getProfileByIdChannel(accountId).consumeEach {
                if (it != null) {
                    profileChannel.send(it)
                } else {
                    closeScreenChannel.send(Unit)
                }
            }
        }
        handleException {
            connectionsInteractor.getStatusesConnections(accountId).consumeEach {
                statusesConnectionsChannel.send(it)
            }
        }
        handleException {
            postsInteractor.loadAllUserPostByAccountIdImmediately(accountId).apply { postChannel.send(ListItemEvent.Added(this)) }
            postsInteractor.loadAllUserPostByAccountId(accountId).map { it.toBatched() }.consumeTo(postChannel)
        }
        handleException {
            profileChannel.consumeEach { profile ->
                val interests = profile.interests.map { async { tagInteractor.get(it) } }.mapNotNull { it.await() }
                interestsChannel.send(interests)

                val offers = profile.offers.map { async { tagInteractor.get(it) } }.mapNotNull { it.await() }
                offersChannel.send(offers)
            }

        }
    }

    override suspend fun retrieveComplaints(): List<TranslatedWordModel> {
        if (reportsList.isNotEmpty()) return reportsList
        showProgress()
        reportsList = complaintInteractor.getReports()
        hideProgress()
        return reportsList
    }

    override fun sendComplaint(id: String, reason: String, authorText: String?) {
        handleException {
            withProgressSuspend {
                complaintInteractor.sendComplaint(ComplaintModelImpl(
                        id = id,
                        type = NetworkContract.Complaint.ACCOUNT_TYPE,
                        reason = reason,
                        authorText = authorText
                ))
            }
        }
    }

    override fun sendConnectionStatus(connectionStatus: ConnectionStatus, aid: String) {
        handleException {
            withProgressSuspend {
                val action = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> ConnectionAction.DISCONNECT
                    ConnectionStatus.SENT -> ConnectionAction.REVOKE
                    ConnectionStatus.REQUESTED -> ConnectionAction.ACCEPT
                    ConnectionStatus.RECOMMENDED -> ConnectionAction.CONNECT
                    else -> throw IllegalArgumentException("Wrong connection status")
                }
                connectionsInteractor.actionConnectionStatus(action, listOf(aid))
            }
        }
    }

    override fun inviteToGroup(group: GroupModel) {
        handleException {
            withProgressSuspend {
                groupsInteractor.sendInvite(groupId = group.id, accountIds = listOf(accountId))
            }
        }
    }
}