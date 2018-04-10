package com.mnassa.screen.profile

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.*
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
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
    override val profileClickChannel: BroadcastChannel<ProfileViewModel.ProfileCommand> = BroadcastChannel(10)
    override val statusesConnectionsChannel: BroadcastChannel<ConnectionStatus> = BroadcastChannel(10)
    override val postChannel: BroadcastChannel<ListItemEvent<PostModel>> = BroadcastChannel(10)
    override val reportsChannel: BroadcastChannel<List<TranslatedWordModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleException {
            val reporst = complaintInteractor.getReports()
            reportsChannel.send(reporst)
        }
    }

    override fun retreiveComplaints() {
        handleException {
            withProgressSuspend {
                val reporst = complaintInteractor.getReports()
                reportsChannel.send(reporst)
            }
        }
    }

    private var profileClickJob: Job? = null
    override fun connectionClick() {
        profileClickJob?.cancel()
        profileClickJob = launchCoroutineUI {
            profileClickChannel.send(ProfileViewModel.ProfileCommand.ProfileConnection())
        }
    }

    private var walletClickJob: Job? = null
    override fun walletClick() {
        walletClickJob?.cancel()
        walletClickJob = launchCoroutineUI {
            profileClickChannel.send(ProfileViewModel.ProfileCommand.ProfileWallet())
        }
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
                            , connectionsInteractor.getConnectedStatusById(accountId))
                    profileChannel.send(profile)
                }
                hideProgress()
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
                        type = ACCOUNT_TYPE,
                        reason = reason
                ))
            }
        }
    }

    override fun connectionStatusClick(connectionStatus: ConnectionStatus) {
        launchCoroutineUI {
            statusesConnectionsChannel.send(connectionStatus)
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

    companion object {
        const val ACCOUNT_TYPE = "account"
    }

}