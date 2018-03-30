package com.mnassa.screen.profile

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ConnectionStatus
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
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
        private val postsInteractor: PostsInteractor
) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val profileClickChannel: BroadcastChannel<ProfileViewModel.ProfileCommand> = BroadcastChannel(10)
    override val statusesConnectionsChannel: ConflatedBroadcastChannel<ConnectionStatus> = ConflatedBroadcastChannel()
    override suspend fun getPostsById(accountId: String): ReceiveChannel<ListItemEvent<PostModel>> = postsInteractor.loadAllUserPostByAccountUd(accountId)

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
            withProgressSuspend {
                val profileAccountModel = userProfileInteractor.getPrifileByAccountId(accountId)
                Timber.i(profileAccountModel.toString())
                profileAccountModel?.let {
                    val profile = ProfileModel(it,
                            tagInteractor.getTagsByIds(it.interests),
                            tagInteractor.getTagsByIds(it.offers),
                            userProfileInteractor.getAccountId() == accountId
                            , connectionsInteractor.getConnectedStatusById(accountId))
                    profileChannel.send(profile)
                }
            }
        }
    }

    override fun connectionStatusClick(connectionStatus: ConnectionStatus) {
//        connectionsInteractor.actionConnection()


    }

    override fun <T> handleException(function: suspend () -> T): Job {
        return super.handleException(function)
    }
}