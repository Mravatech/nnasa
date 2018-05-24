package com.mnassa.screen.group.details

import android.os.Bundle
import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 5/14/2018.
 */
class GroupDetailsViewModelImpl(private val groupId: String,
                                private val groupsInteractor: GroupsInteractor,
                                private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), GroupDetailsViewModel {

    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val tagsChannel: BroadcastChannel<List<TagModel>> = ConflatedBroadcastChannel()
    override val membersChannel: BroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()
    override val closeScreenChannel: BroadcastChannel<Unit> = ArrayBroadcastChannel(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) {
                    groupChannel.send(it)
                    tagsChannel.send(loadTags(it.tags))

                } else closeScreenChannel.send(Unit)
            }
        }
        handleException {
            groupsInteractor.getGroupMembers(groupId).consumeTo(membersChannel)
        }
    }

    private suspend fun loadTags(tags: List<String>): List<TagModel> {
        return tags.map { tag -> asyncWorker { tagInteractor.get(tag) } }.mapNotNull { it.await() }
    }
}