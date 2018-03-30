package com.mnassa.screen.profile

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import com.mnassa.screen.profile.model.ProfileModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val connectionsInteractor: ConnectionsInteractor
) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val profileClickChannel: BroadcastChannel<ProfileViewModel.ProfileCommand> = BroadcastChannel(10)
    override val statusesConnectionsChannel: BroadcastChannel<String?> = BroadcastChannel(10)

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        handleException {
//            connectionsInteractor.getStatusesConnections().consumeEach {
//                statusesConnectionsChannel.send(it)
//            }
//        }
//    }

    private var profileClickJob: Job? = null
    override fun connectionClick() {
        profileClickJob?.cancel()
        profileClickJob = launchCoroutineUI {
            profileClickChannel.send(ProfileViewModel.ProfileCommand.ProfileConnection())
        }
    }

    private var walletClickJob: Job? = null
    override fun walletClick() {
        walletClickJob = launchCoroutineUI {
            profileClickChannel.send(ProfileViewModel.ProfileCommand.ProfileWallet())
        }
    }

    override fun getProfileWithAccountId(accountId: String) {
        handleException {
            var profileAccountModel: ProfileAccountModel? = null
            withProgressSuspend {
                profileAccountModel = userProfileInteractor.getPrifileByAccountId(accountId)
                Timber.i(profileAccountModel.toString())
            }
            profileAccountModel?.let {
                val profile = ProfileModel(it,
                        tagInteractor.getTagsByIds(it.interests),
                        tagInteractor.getTagsByIds(it.offers),
                        userProfileInteractor.getAccountId() == accountId)
                profileChannel.send(profile)
                if (profile.isMyProfile) return@handleException
                val status = connectionsInteractor.getStatusesConnections(accountId)
                statusesConnectionsChannel.send(status)
            }
        }
    }
}