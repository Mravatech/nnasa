package com.mnassa.screen.profile

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

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
        private val complaintInteractor: ComplaintInteractor
) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: ConflatedBroadcastChannel<ProfileAccountModel> = ConflatedBroadcastChannel()
    override val statusesConnectionsChannel: ConflatedBroadcastChannel<ConnectionStatus> = ConflatedBroadcastChannel()
    override val postChannel: ConflatedBroadcastChannel<ListItemEvent<PostModel>> = ConflatedBroadcastChannel()
    override val interestsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val offersChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            reportsList = complaintInteractor.getReports()
        }

        handleException {
            userProfileInteractor.getProfileByIdChannel(accountId).consumeTo(profileChannel)
        }
        handleException {
            connectionsInteractor.getStatusesConnections(accountId).consumeEach {
                statusesConnectionsChannel.send(it)
            }
        }
        handleException {
            postsInteractor.loadAllUserPostByAccountId(accountId).consumeEach {
                postChannel.send(it)
            }
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
}