package com.mnassa.screen.profile

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
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
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val profileClickChannel: BroadcastChannel<ProfileViewModel.ProfileCommand> = BroadcastChannel(10)

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

    private var profileJob: Job? = null
    override fun getProfileWithAccountId(accountId: String) {
        profileJob?.cancel()
        profileJob = handleException {
            withProgressSuspend {
                val profileAccountModel = userProfileInteractor.getPrifileByAccountId(accountId)
                Timber.i(profileAccountModel.toString())
                if (profileAccountModel != null) {
                    val profile = ProfileModel(profileAccountModel,
                            tagInteractor.getTagsByIds(profileAccountModel.interests),
                            tagInteractor.getTagsByIds(profileAccountModel.offers),
                            userProfileInteractor.getAccountId() == accountId)
                    profileChannel.send(profile)
                }

            }
        }

    }
}