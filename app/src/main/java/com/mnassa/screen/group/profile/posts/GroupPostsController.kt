package com.mnassa.screen.group.profile.posts

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.isAdmin
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.subscribeToUpdates
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.PostsRVAdapter
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_group_profile_posts.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 09.08.2018.
 */
class GroupPostsController(args: Bundle) : MnassaControllerImpl<GroupPostsViewModel>() {
    override val layoutId: Int = R.layout.controller_group_profile_posts
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    override val viewModel: GroupPostsViewModel by instance(arg = groupId)

    private val adapter = PostsRVAdapter(withHeader = false)
    private val popupMenuHelper: PopupMenuHelper by instance()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        adapter.onAttachedToWindow = { post -> viewModel.onAttachedToWindow(post) }
        adapter.onItemClickListener = {
            val postDetailsFactory: PostDetailsFactory by instance()
            open(postDetailsFactory.newInstance(it))
        }
        adapter.onRepostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onPostedByClickListener = { open(ProfileController.newInstance(it)) }
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onHideInfoPostClickListener = viewModel::hideInfoPost
        adapter.onMoreItemClickListener = this::showPostMenu
        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.newsFeedChannel.subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().rlEmptyView }
            )
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            rvGroupPosts.adapter = adapter
        }

        launchCoroutineUI { viewModel.groupChannel.consumeEach { bindGroup(it, view) } }
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        outState.putInt(EMPTY_STATE_VISIBILITY, view.rlEmptyView.visibility)
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        view.rlEmptyView.visibility = savedViewState.getInt(EMPTY_STATE_VISIBILITY, view.rlEmptyView.visibility)
    }

    override fun onDestroyView(view: View) {
//        view.rvGroupTags.adapter = null
        view.rvGroupPosts.adapter = null
        super.onDestroyView(view)
    }

    private fun bindGroup(group: GroupModel, view: View) {
        adapter.showMoreOptions = group.isAdmin
    }

    private fun showPostMenu(post: PostModel, view: View) {
        popupMenuHelper.showGroupPostItemMenu(view,
                onRemove = { viewModel.removePost(post) }
        )
    }

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EMPTY_STATE_VISIBILITY = "EMPTY_STATE_VISIBILITY"

        fun newInstance(groupId: String): GroupPostsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, groupId)
            return GroupPostsController(args)
        }
    }
}