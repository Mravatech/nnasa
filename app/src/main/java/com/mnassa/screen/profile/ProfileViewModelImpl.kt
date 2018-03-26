package com.mnassa.screen.profile

import com.google.firebase.storage.FirebaseStorage
import com.mnassa.domain.interactor.OtherProfileInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.TagModel
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
        private val storageInteractor: StorageInteractor,
        private val storage: FirebaseStorage,
        private val tagInteractor: TagInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val profileInteractor: OtherProfileInteractor) : MnassaViewModelImpl(), ProfileViewModel {

    override val profileChannel: BroadcastChannel<ProfileModel> = BroadcastChannel(10)
    override val tagChannel: BroadcastChannel<List<TagModel>> = BroadcastChannel(10)

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        handleException {
//            withProgressSuspend {
//                val profileAccountModel = profileInteractor.getPrifileByAccountId("-L7iL1VRfulD0PIQBT7V"//"-L8CtC3Vst4AcfsP67lf"-L7iL1VRfulD0PIQBT7V
//                )
//                Timber.i(profileAccountModel.toString())
//                if (profileAccountModel != null) {
//                    val profile = ProfileModel(profileAccountModel,
//                            tagInteractor.getTagsByIds(profileAccountModel.interests
//                                    ?: emptyList()),
//                            tagInteractor.getTagsByIds(profileAccountModel.offers
//                                    ?: emptyList()),
//                            userProfileInteractor.getAccountId() == "-L7iL1VRfulD0PIQBT7V")
//                    profileChannel.send(profile)
//                }
//
//            }
//        }
//    }

    private var profileJob: Job? = null
    override fun getProfileWithAccountId(accountId: String) {
        profileJob?.cancel()
        profileJob = handleException {
            withProgressSuspend {
                val profileAccountModel = profileInteractor.getPrifileByAccountId(accountId)
                Timber.i(profileAccountModel.toString())
                if (profileAccountModel != null) {
                    val profile = ProfileModel(profileAccountModel,
                            tagInteractor.getTagsByIds(profileAccountModel.interests
                                    ?: emptyList()),
                            tagInteractor.getTagsByIds(profileAccountModel.offers
                                    ?: emptyList()),
                            userProfileInteractor.getAccountId() == "-L7iL1VRfulD0PIQBT7V")
                    profileChannel.send(profile)
                }

            }
        }

    }
}