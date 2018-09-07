package com.mnassa.screen.home

import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.PermissionsModel
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.ProcessAccountChangeArrayBroadcastChannel
import com.mnassa.extensions.ProcessAccountChangeConflatedBroadcastChannel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.produce

/**
 * Created by Peter on 3/6/2018.
 */
class HomeViewModelImpl(private val countersInteractor: CountersInteractor,
                        private val userProfileInteractor: UserProfileInteractor,
                        private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), HomeViewModel {

    override val unreadEventsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadEvents
    }

    override val unreadNeedsCountChannel: ConflatedBroadcastChannel<Int> by ProcessAccountChangeConflatedBroadcastChannel {
        countersInteractor.numberOfUnreadNeeds
    }

    override val permissionsChannel: ConflatedBroadcastChannel<PermissionsModel> by ProcessAccountChangeConflatedBroadcastChannel {
        userProfileInteractor.getPermissions()
    }

    override val showAddTagsDialog: BroadcastChannel<Unit> by ProcessAccountChangeArrayBroadcastChannel(
            receiveChannelProvider = {
                produce {
                    if (tagInteractor.shouldShowAddTagsDialog()) send(Unit)
                }
            }
    )

    override suspend fun getInterests(): List<TagModel> {
        return handleExceptionsSuspend { tagInteractor.get(getProfile()?.interests ?: emptyList()) }
                ?: emptyList()
    }

    override suspend fun getOffers(): List<TagModel> {
        return handleExceptionsSuspend { tagInteractor.get(getProfile()?.offers ?: emptyList()) }
                ?: emptyList()
    }

    override suspend fun getProfile(): ProfileAccountModel? {
        return handleExceptionsSuspend {
            userProfileInteractor.getProfileByIdChannel(userProfileInteractor.getAccountIdOrException()).consume { receive() }
        }

    }
}