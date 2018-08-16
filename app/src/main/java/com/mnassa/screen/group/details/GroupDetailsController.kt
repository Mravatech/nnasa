package com.mnassa.screen.group.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.data.network.bean.retrofit.request.GroupConnectionRequest
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.invite.GroupInviteConnectionsController
import com.mnassa.screen.group.members.GroupMembersController
import com.mnassa.screen.group.profile.GroupProfileController
import com.mnassa.screen.group.requests.GroupConnectionRequestsController
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.screen.wallet.WalletController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_group_details.view.*
import kotlinx.android.synthetic.main.item_group_member_round.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupDetailsController(args: Bundle) : MnassaControllerImpl<GroupDetailsViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_details
    private val groupId: String = args.getString(EXTRA_GROUP_ID)
    private var group = args.getSerializable(EXTRA_GROUP) as GroupModel
    override val viewModel: GroupDetailsViewModel by instance(arg = groupId)
    private val tagsAdapter = PostTagRVAdapter()
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val dialogHelper: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }
        launchCoroutineUI { viewModel.groupChannel.consumeEach { bindGroup(it, view) } }
        launchCoroutineUI { viewModel.tagsChannel.consumeEach { bindTags(it, view) } }
        launchCoroutineUI { viewModel.membersChannel.consumeEach { bindMembers(it, view) } }
        launchCoroutineUI { viewModel.pointsChannel.consumeEach { view.tvPointsCount.text = it.toString() } }
        launchCoroutineUI {
            viewModel.isMemberChannel.consumeEach { isMember ->
                view.btnOpenGroup.isGone = !isMember
                view.vBtnOpenGroupShadow.visibility = view.btnOpenGroup.visibility
            }
        }
        launchCoroutineUI {
            viewModel.hasInviteChannel.consumeEach { hasInvite ->
                if (hasInvite) {
                    view.toolbar.onMoreClickListener = { view ->
                        popupMenuHelper.showGroupInviteMenu(view, { viewModel.acceptInvite() }, { viewModel.declineInvite() })
                    }
                } else {
                    view.toolbar.onMoreClickListener = null
                }

            }
        }

        with(view) {
            rvGroupTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                    .setScrollingEnabled(false)
                    .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            rvGroupTags.adapter = tagsAdapter
            btnOpenGroup.text = fromDictionary(R.string.group_open_profile)

            llGroupMembersCounter.setOnClickListener { open(GroupMembersController.newInstance(group)) }
            llGroupInvitesCounter.setOnClickListener { if (group.isAdmin) open(GroupInviteConnectionsController.newInstance(group)) }
            llGroupPointsCounter.setOnClickListener { if (group.isAdmin) open(WalletController.newInstanceGroup(group)) }
        }

        bindGroup(group, view)
    }

    override fun onDestroyView(view: View) {
        view.rvGroupTags.adapter = null
        super.onDestroyView(view)
    }

    private fun bindGroup(group: GroupModel, view: View) {
        this.group = group
        with(view) {
            //avatar
            val avatar = group.avatar
            ivGroupAvatar.avatarSquare(avatar)
            if (avatar != null && !avatar.isBlank()) {
                ivGroupAvatar.setOnClickListener {
                    PhotoPagerActivity.start(view.context, listOf(avatar), 0)
                }
            }

            //counters
            tvMembersCount.text = group.numberOfParticipants.toString()
            tvInvitesCount.text = group.numberOfInvites.toString()

            //titles
            tvGroupTitle.text = group.name
            launchCoroutineUI {
                tvGroupSubTitle.text = group.formattedRole(viewModel.isMemberChannel.consume { receive() })
            }

            //description
            tvGroupDescription.text = group.description
            tvGroupDescription.goneIfEmpty()
            vGroupDescriptionDivider.visibility = tvGroupDescription.visibility
            tvGroupDescriptionHeader.visibility = tvGroupDescription.visibility

            //website
            tvGroupWebsite.text = group.website
            tvGroupWebsite.goneIfEmpty()
            tvGroupWebsiteHeader.visibility = tvGroupWebsite.visibility
            vGroupWebsiteDivider.visibility = tvGroupWebsite.visibility

            //location
            tvGroupLocation.text = group.locationPlace?.placeName?.toString()
            tvGroupLocation.goneIfEmpty()
            tvGroupLocationHeader.visibility = tvGroupLocation.visibility
            vGroupLocationDivider.visibility = tvGroupLocation.visibility

            //open group button
            btnOpenGroup.setOnClickListener {
                open(GroupProfileController.newInstance(group))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindMembers(members: List<ShortAccountModel>, view: View) {
        with(view) {
            llGroupMembers.isGone = members.isEmpty()
            tvGroupMembersHeader.visibility = llGroupMembers.visibility
            vGroupMembersDivider.visibility = llGroupMembers.visibility

            llGroupMembersContainer.removeAllViews()
            val itemSize = view.context.resources.getDimensionPixelSize(R.dimen.group_details_member_avatar_size)
            val inflater = LayoutInflater.from(view.context)

            val itemsToInflate = minOf(members.size, MAX_MEMBERS_TO_SHOW)
            for (i in 0 until itemsToInflate) {
                val itemView = inflater.inflate(R.layout.item_group_member_round, llGroupMembersContainer, false)
                llGroupMembersContainer.addView(itemView)

                val offset = i.toDouble() * (itemSize.toDouble() * GROUP_MEMBERS_ITEMS_DISTANCE)
                val layoutParams = itemView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginStart = offset.toInt()
                itemView.layoutParams = layoutParams

                itemView.ivGroupMemberAvatar.avatarRound(members[i].avatar)
                itemView.setOnClickListener { open(ProfileController.newInstance(members[i])) }
            }

            val moreItemsCount = members.size - itemsToInflate
            tvGroupMembersCounter.text = "+$moreItemsCount"
            tvGroupMembersCounter.isGone = moreItemsCount == 0
            tvGroupMembersCounter.setOnClickListener { open(GroupMembersController.newInstance(group)) }
        }
    }

    private fun bindTags(tags: List<TagModel>, view: View) {
        with(view) {
            tagsAdapter.set(tags)

            rvGroupTags.isGone = tags.isEmpty()
            tvGroupTagsHeader.visibility = rvGroupTags.visibility
            vGroupTagsDivider.visibility = rvGroupTags.visibility
        }
    }

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val MAX_MEMBERS_TO_SHOW = 5
        private const val GROUP_MEMBERS_ITEMS_DISTANCE = 0.75

        fun newInstance(group: GroupModel): GroupDetailsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupDetailsController(args)
        }
    }
}