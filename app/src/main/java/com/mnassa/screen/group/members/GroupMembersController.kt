package com.mnassa.screen.group.members

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.isInvisible
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_group_members.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupMembersController(args: Bundle) : MnassaControllerImpl<GroupMembersViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_members
    private val groupId by lazy { args.getString(EXTRA_GROUP_ID) }
    private var group = args.getSerializable(EXTRA_GROUP) as GroupModel
    override val viewModel: GroupMembersViewModel by instance(arg = groupId)
    private val adapter = GroupMembersAdaper()
    private val popupMenuHelper: PopupMenuHelper by instance()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        adapter.isLoadingEnabled = savedInstanceState == null
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvGroupConnections.adapter = adapter
            searchView.etSearch.addTextChangedListener(SimpleTextWatcher {
                adapter.searchByName(it)
            })
        }

        adapter.onItemOptionsClickListener = { shortAccountModel, view -> showGroupMemberMenu(shortAccountModel, view) }
        adapter.onItemClickListener = { open(ProfileController.newInstance(it.user)) }

        launchCoroutineUI {
            viewModel.groupMembersChannel.consumeEach {
                adapter.set(it)
                adapter.isLoadingEnabled = false
                view.rlEmptyView.isInvisible = it.isNotEmpty()
                view.rvGroupConnections.isInvisible = it.isEmpty()
            }
        }

        launchCoroutineUI { viewModel.groupChannel.consumeEach { group = it } }
    }

    private fun showGroupMemberMenu(member: GroupMemberItem, sender: View) {
        val account = member.user
        popupMenuHelper.showGroupMemberMenu(sender, group, account,
                onRemove = { viewModel.removeMember(account) },
                onAdmin = { viewModel.makeAdmin(account) },
                onMember = { viewModel.unMakeAdmin(account) }
        )
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvGroupConnections.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance(group: GroupModel): GroupMembersController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupMembersController(args)
        }
    }
}