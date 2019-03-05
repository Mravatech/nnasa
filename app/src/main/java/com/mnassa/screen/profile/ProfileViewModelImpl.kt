package com.mnassa.screen.profile

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.*
import com.mnassa.domain.interactor.impl.PostsInteractorImpl
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.domain.pagination.PaginationController
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.math.min

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

    override val postsPagination = PaginationController(WALL_INITIAL_SIZE)

    override val profileChannel: ConflatedBroadcastChannel<ProfileAccountModel> = ConflatedBroadcastChannel()
    override val statusesConnectionsChannel: ConflatedBroadcastChannel<ConnectionStatus> = ConflatedBroadcastChannel()
    override val postChannel: ConflatedBroadcastChannel<ListItemEvent<List<PostModel>>> = ConflatedBroadcastChannel()
    override val interestsChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val offersChannel: ConflatedBroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveExceptions {
            reportsList = complaintInteractor.getReports()
        }

        resolveExceptions {
            userProfileInteractor.getProfileByIdChannel(accountId).consumeEach {
                if (it != null) {
                    profileChannel.send(it)
                } else {
                    closeScreenChannel.send(Unit)
                }
            }
        }
        resolveExceptions {
            connectionsInteractor.getStatusesConnections(accountId).consumeEach {
                statusesConnectionsChannel.send(it)
            }
        }
        resolveExceptions {
            postsInteractor.loadWallWithChangesHandling(accountId, postsPagination).consumeTo(postChannel)
        }
        resolveExceptions {
            profileChannel.consumeEach { profile ->
                val interests = handleExceptionsSuspend { tagInteractor.get(profile.interests) } ?: emptyList()
                interestsChannel.send(interests)

                val offers = handleExceptionsSuspend { tagInteractor.get(profile.offers) } ?: emptyList()
                offersChannel.send(offers)
            }

        }
    }

    override fun onScroll(visibleItemCount: Int, totalItemCount: Int, firstVisibleItemPosition: Int) {
        val paginationSize = min(postsPagination.size, totalItemCount.toLong())
        if (visibleItemCount + firstVisibleItemPosition >= paginationSize && firstVisibleItemPosition >= 0) {
            postsPagination.requestNextPage(WALL_PAGE_SIZE)
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
        resolveExceptions {
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
        resolveExceptions {
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
        resolveExceptions {
            withProgressSuspend {
                groupsInteractor.sendInvite(groupId = group.id, accountIds = listOf(accountId))
            }
        }
    }

    companion object {
        private const val WALL_INITIAL_SIZE = 20L
        private const val WALL_PAGE_SIZE = 50L
    }
}