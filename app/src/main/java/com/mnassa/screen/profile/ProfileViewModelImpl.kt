package com.mnassa.screen.profile

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ConnectionAction
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val connectionsInteractor: ConnectionsInteractor,
        private val postsInteractor: PostsInteractor,
        private val complaintInteractor: ComplaintInteractor
) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val statusesConnectionsChannel: BroadcastChannel<ConnectionStatus> = BroadcastChannel(10)
    override val postChannel: BroadcastChannel<ListItemEvent<PostModel>> = BroadcastChannel(10)

    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            reportsList = complaintInteractor.getReports()
        }
    }

    override suspend fun retrieveComplaints(): List<TranslatedWordModel> {
        if (reportsList.isNotEmpty()) return reportsList
        handleException {
            withProgressSuspend {
                reportsList = complaintInteractor.getReports()
            }
        }
        return reportsList
    }

    override fun getProfileWithAccountId(accountId: String) {
        handleException {
            showProgress()
            userProfileInteractor.getProfileById(accountId).consumeEach {
                val profileAccountModel = it
                Timber.i(profileAccountModel.toString())
                profileAccountModel?.let {
                    val profile = ProfileModel(it,
                            tagInteractor.getTagsByIds(it.interests),
                            tagInteractor.getTagsByIds(it.offers),
                            userProfileInteractor.getAccountId() == accountId
                            , connectionsInteractor.getConnectionStatusById(accountId))
                    profileChannel.send(profile)
                }
                hideProgress()
            }
        }
        handleException {
            connectionsInteractor.getStatusesConnections(accountId).consumeEach {
                statusesConnectionsChannel.send(it)
            }
        }
    }

    override fun getPostsById(accountId: String) {
        handleException {
            postsInteractor.loadAllUserPostByAccountId(accountId).consumeEach {
                postChannel.send(it)
            }
        }
    }

    override fun sendComplaint(id: String, reason: String) {
        handleException {
            withProgressSuspend {
                complaintInteractor.sendComplaint(ComplaintModelImpl(
                        id = id,
                        type = NetworkContract.Complaint.ACCOUNT_TYPE,
                        reason = reason
                ))
            }
        }
    }

    override fun sendConnectionStatus(connectionStatus: ConnectionStatus, aid: String, isAcceptConnect: Boolean) {
        handleException {
            withProgressSuspend {
                val action = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> ConnectionAction.DISCONNECT
                    ConnectionStatus.SENT -> ConnectionAction.REVOKE
                    ConnectionStatus.REQUESTED -> if (isAcceptConnect) ConnectionAction.ACCEPT else ConnectionAction.DECLINE
                    ConnectionStatus.RECOMMENDED -> ConnectionAction.CONNECT
                    else -> throw IllegalArgumentException("Wrong connection status")
                }
                connectionsInteractor.actionConnectionStatus(action, listOf(aid))
            }
        }
    }
}