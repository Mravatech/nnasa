package com.mnassa.screen.group.details

import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 5/14/2018.
 */
class GroupDetailsViewModelImpl(private val groupId: String,
                                private val groupsInteractor: GroupsInteractor,
                                private val tagInteractor: TagInteractor,
                                private val userProfileInteractor: UserProfileInteractor,
                                private val walletInteractor: WalletInteractor) : MnassaViewModelImpl(), GroupDetailsViewModel {

    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val pointsChannel: BroadcastChannel<Long> = ConflatedBroadcastChannel()
    override val isMemberChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel()
    override val tagsChannel: BroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val membersChannel: BroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val hasInviteChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    override val openScreenChannel: BroadcastChannel<GroupDetailsViewModel.ScreenToOpen> = BroadcastChannel(1)

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) {
                    groupChannel.send(it)
                    tagsChannel.send(loadTags(it.tags))

                } else closeScreenChannel.send(Unit)
            }
        }
        setupScope.launchWorker {
            groupsInteractor.getGroupMembers(groupId).consumeTo(membersChannel)
        }
        setupScope.launchWorker {
            membersChannel.consumeEach { members ->
                val userId = userProfileInteractor.getAccountIdOrException()
                isMemberChannel.send(members.any { it.id == userId })
            }
        }
        setupScope.launchWorker {
            groupsInteractor.getHasInviteToGroupChannel(groupId).consumeTo(hasInviteChannel)
        }
        setupScope.launchWorker {
            walletInteractor.getGroupBalance(groupId).consumeTo(pointsChannel)
        }
    }

    override fun acceptInvite() {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.acceptInvite(groupId)
                openScreenChannel.send(GroupDetailsViewModel.ScreenToOpen.GROUP_PROFILE)
            }
        }
    }

    override fun declineInvite() {
        launchWorker {
            withProgressSuspend {
                groupsInteractor.declineInvite(groupId)
                closeScreenChannel.send(Unit)
            }
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> = handleExceptionsSuspend { tagInteractor.get(tags) } ?: emptyList()
}