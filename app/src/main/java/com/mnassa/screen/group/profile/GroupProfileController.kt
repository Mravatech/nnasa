package com.mnassa.screen.group.profile

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.bufferize
import com.mnassa.extensions.avatarRound
import com.mnassa.extensions.formattedName
import com.mnassa.extensions.formattedRole
import com.mnassa.extensions.isGone
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.members.GroupMembersController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.posts.general.create.CreateGeneralPostController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.offer.create.CreateOfferController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_group_profile.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupProfileController(args: Bundle) : MnassaControllerImpl<GroupProfileViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_profile
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    override val viewModel: GroupProfileViewModel by instance(arg = groupId)
    private val adapter = PostsRVAdapter()
    private var groupModel: GroupModel? = null


    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        adapter.onAttachedToWindow = { post -> viewModel.onAttachedToWindow(post) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onCreateNeedClickListener = {
            launchCoroutineUI {
                if (viewModel.permissionsChannel.consume { receive() }.canCreateNeedPost) {
                    open(CreateNeedController.newInstance(groupId = groupId))
                }
            }
        }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onPostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onHideInfoPostClickListener = { viewModel.hideInfoPost(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view)

        with(view) {
            toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
            llGroupMembers.setOnClickListener { groupModel?.let { open(GroupMembersController.newInstance(it)) } }
        }

        launchCoroutineUI {
            viewModel.groupChannel.consumeEach { bindGroup(it, view) }
        }

        launchCoroutineUI { viewModel.closeScreenChannel.consumeEach { close() } }


        ///
        view.rvGroupPosts.adapter = adapter

        adapter.isLoadingEnabled = savedInstanceState == null
        launchCoroutineUI {
            viewModel.newsFeedChannel.openSubscription().bufferize(this@GroupProfileController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        if (it.item.isNotEmpty()) {
                            adapter.dataStorage.addAll(it.item)
                        }
                        adapter.isLoadingEnabled = it.item.isEmpty() && adapter.dataStorage.isEmpty()
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = true
                    }
                }
            }
        }

        launchCoroutineUI {
            viewModel.infoFeedChannel.openSubscription().bufferize(this@GroupProfileController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        if (it.item.isNotEmpty()) {
                            adapter.dataStorage.addAll(it.item)
                        }
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                }
            }
        }

        ///

        initFab(view)

        if (args.containsKey(EXTRA_GROUP)) {
            bindGroup(args.getSerializable(EXTRA_GROUP) as GroupModel, view)
            args.remove(EXTRA_GROUP)
        }
    }

    private fun initFab(view: View) {
        with(view) {
            fabGroup.setClosedOnTouchOutside(true)
        }

        launchCoroutineUI {
            viewModel.permissionsChannel.consumeEach { permission ->
                with(getViewSuspend()) {
                    fabGroup.removeAllMenuButtons()

                    if (permission.canCreateGeneralPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_general_post))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateGeneralPostController.newInstance(groupId = groupId))
                        }
                        fabGroup.addMenuButton(button)
                    }

//                    if (permission.canCreateEvent) {
//                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_event))
//                        button.setOnClickListener {
//                            fabGroup.close(false)
//                            open(CreateEventController.newInstance(groupId = groupId))
//                        }
//                        fabGroup.addMenuButton(button)
//                    }

                    if (permission.canCreateOfferPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_offer))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateOfferController.newInstance(groupId = groupId))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    if (permission.canCreateNeedPost) {
                        val button = inflateMenuButton(fromDictionary(R.string.tab_home_button_create_need))
                        button.setOnClickListener {
                            fabGroup.close(false)
                            open(CreateNeedController.newInstance(groupId = groupId))
                        }
                        fabGroup.addMenuButton(button)
                    }

                    fabGroup.isGone = !(
                            permission.canCreateNeedPost ||
                                    permission.canCreateOfferPost ||
                                    permission.canCreateEvent ||
                                    permission.canCreateGeneralPost)
                }
            }
        }
    }

    private fun View.inflateMenuButton(text: String): FloatingActionButton {
        val button = FloatingActionButton(context)
        button.buttonSize = FloatingActionButton.SIZE_MINI
        button.setImageResource(R.drawable.ic_edit_white_24dp)
        button.colorNormal = ContextCompat.getColor(context, R.color.accent)
        button.colorPressed = ContextCompat.getColor(context, R.color.tealish)
        button.labelText = text

        return button
    }

    private fun bindGroup(group: GroupModel, view: View) {
        this.groupModel = group
        with(view) {
            ivGroupAvatar.avatarRound(group.avatar)
            tvGroupTitle.text = group.formattedName
            tvGroupSubTitle.text = group.formattedRole
            toolbar.title = tvGroupTitle.text

            tvMembersCount.text = group.numberOfParticipants.toString()
        }
    }

    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance(group: GroupModel): GroupProfileController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP, group)

            return GroupProfileController(args)
        }
    }
}