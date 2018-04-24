package com.mnassa.screen.posts.need.details

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.comments.CommentsWrapperListener
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.adapter.PhotoPagerAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.screen.posts.need.recommend.RecommendController
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.controller_need_details_header.view.*
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.runBlocking
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/19/2018.
 */
open class NeedDetailsController(args: Bundle) : MnassaControllerImpl<NeedDetailsViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult,
        ComplaintOtherController.OnComplaintResult,
        CommentsWrapperController.CommentsWrapperCallback,
        RecommendController.OnRecommendPostResult {
    override val layoutId: Int = R.layout.controller_need_details_header
    protected val postId by lazy { requireNotNull(args.getString(EXTRA_NEED_ID)) }
    protected var post: PostModel? = null
    override val viewModel: NeedDetailsViewModel by instance(arg = postId)
    override var sharingOptions: SharingOptionsController.ShareToOptions = SharingOptionsController.ShareToOptions.EMPTY
        set(value) = viewModel.repost(value)
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val dialogHelper: DialogHelper by instance()
    private val tagsAdapter = PostTagRVAdapter()
    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(postId, OTHER, value)
        }
    private val commentsWrapper by lazy { parentController as CommentsWrapperListener }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                    .setScrollingEnabled(false)
                    .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            rvTags.adapter = tagsAdapter
        }

        launchCoroutineUI { viewModel.postChannel.consumeEach { bindPost(it) } }

        launchCoroutineUI { viewModel.postTagsChannel.consumeEach { bindTags(it) } }

        launchCoroutineUI { viewModel.finishScreenChannel.consumeEach { close() } }

        (args.getSerializable(EXTRA_NEED_MODEL) as PostModel?)?.let { post ->
            runBlocking { bindPost(post) }
            args.remove(EXTRA_NEED_MODEL)
        }
    }

    override fun onDestroyView(view: View) {
        view.rvTags.adapter = null
        super.onDestroyView(view)
    }

    override fun onRecommendedAccountResult(recommendedAccounts: List<ShortAccountModel>) = commentsWrapper.onRecommendedAccountResult(recommendedAccounts)
    override val recommendedAccounts: List<ShortAccountModel> get() = commentsWrapper.recommendedAccounts

    private fun complainAboutProfile(view: View) {
        launchCoroutineUI {
            val reportsList = viewModel.retrieveComplaints()
            dialogHelper.showComplaintDialog(view.context, reportsList) {
                if (it.id == OTHER) {
                    val controller = ComplaintOtherController.newInstance()
                    controller.targetController = this@NeedDetailsController
                    open(controller)
                } else {
                    viewModel.sendComplaint(postId, it.id, null)
                }
            }
        }
    }

    protected open fun showMyPostMenu(view: View, post: PostModel) {
        popupMenuHelper.showMyPostMenu(
                view = view,
                onEditPost = { open(CreateNeedController.newInstanceEditMode(post)) },
                onDeletePost = { viewModel.delete() })
    }

    private fun showOtherUserPostMenu(view: View) {
        post?.let {
            popupMenuHelper.showPostMenu(
                    view = view,
                    post = it,
                    onRepost = { openSharingOptionsScreen() },
                    onReport = { complainAboutProfile(view) }
            )
        }
    }

    protected open suspend fun bindPost(post: PostModel) {
        this.post = post

        with(getViewSuspend()) {
            //author block
            ivAvatar.avatarRound(post.author.avatar)
            tvUserName.text = post.author.formattedName
            tvPosition.text = post.author.formattedPosition
            tvPosition.goneIfEmpty()
            tvEventName.text = post.author.formattedFromEvent
            tvEventName.goneIfEmpty()
            ivChat.setOnClickListener { open(ChatMessageController.newInstance(post, post.author)) }
            rlCreatorRoot.setOnClickListener { open(ProfileController.newInstance(post.author)) }

            //
            tvNeedDescription.text = post.formattedText
            tvNeedDescription.goneIfEmpty()
            //images
            flImages.isGone = post.images.isEmpty()
            if (post.images.isNotEmpty()) {
                pivImages.count = post.images.size
                pivImages.selection = 0

                vpImages.adapter = PhotoPagerAdapter(post.images) {
                    PhotoPagerActivity.start(context, post.images, post.images.indexOf(it))
                }
                vpImages.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        pivImages.selection = position
                    }
                })
            }
            //price
            tvPrice.visibility = if (post.price > 0.0) View.VISIBLE else View.GONE
            tvPrice.text = post.price.formatAsMoneySAR()

            //location
            tvLocation.text = post.locationPlace.formatted()
            tvLocation.invisibleIfEmpty()
            //time
            tvCreationTime.text = post.createdAt.toTimeAgo()

            //views count
            tvViewsCount.text = fromDictionary(R.string.need_views_count).format(post.counters.views)
            ivRepost.setOnClickListener { openSharingOptionsScreen() }
            tvRepostsCount.text = post.counters.reposts.toString()

            btnComment.text = fromDictionary(R.string.need_comment_button)
            btnComment.setOnClickListener { commentsWrapper.openKeyboardOnComment() }

            val recommendWithCount = StringBuilder(fromDictionary(R.string.need_recommend_button))
            if (post.autoSuggest.total > 0) {
                recommendWithCount.append(" (")
                recommendWithCount.append(post.autoSuggest.total.toString())
                recommendWithCount.append(") ")
            }

            btnRecommend.text = recommendWithCount
            btnRecommend.setOnClickListener { openRecommendScreen(post, recommendedAccounts.map { it.id }) }

            tvCommentsCount.setHeaderWithCounter(R.string.need_comments_count, post.counters.comments)
        }
    }

    protected open suspend fun bindTags(tags: List<TagModel>) {
        getViewSuspend().let {
            with(it) {
                vTagsSeparator.isGone = tags.isEmpty()
                rvTags.isGone = tags.isEmpty()
                tagsAdapter.set(tags)
            }
        }
    }

    override fun bindToolbar(toolbar: MnassaToolbar) {
        launchCoroutineUI {
            val post = viewModel.postChannel.openSubscription().consume { receive() }
            toolbar.title = fromDictionary(R.string.need_details_title).format(post.author.formattedName)
            if (post.isMyPost()) {
                toolbar.onMoreClickListener = { showMyPostMenu(it, post) }
                makePostActionsGone()
            } else {
                toolbar.onMoreClickListener = { showOtherUserPostMenu(it) }
                makePostActionsVisible()
            }
        }
    }

    override fun openRecommendScreen(recommendedAccountIds: List<String>, self: CommentsWrapperController) {
        post?.let { openRecommendScreen(it, recommendedAccountIds) }
    }

    private fun openRecommendScreen(post: PostModel, recommendedAccountIds: List<String>) {
        open(RecommendController.newInstance(
                bestMatchesAccounts = post.autoSuggest.accountIds,
                selectedAccounts = recommendedAccountIds,
                excludedAccounts = listOf(post.author.id),
                listener = this))
    }

    override fun bindCanReadComments(canReadComments: Boolean) {
        //TODO
    }

    override fun bindCanWriteComments(canWriteComments: Boolean) {
        launchCoroutineUI {
            if (!canWriteComments) {
                makePostActionsGone()
            }
        }
    }

    protected open suspend fun makePostActionsGone() {
        getViewSuspend().let {
            it.llOtherPersonPostActions.isGone = true
            it.vOtherPersonPostActionsSeparator.isGone = true
        }
    }

    protected open suspend fun makePostActionsVisible() {
        getViewSuspend().let {
            it.llOtherPersonPostActions.visibility = View.VISIBLE
            it.vOtherPersonPostActionsSeparator.visibility = View.VISIBLE
        }
    }

    private fun openSharingOptionsScreen() {
        if (post?.canBeShared == true) {
            open(SharingOptionsController.newInstance(listener = this))
        }
    }

    companion object {
        const val EXTRA_NEED_ID = "EXTRA_NEED_ID"
        const val EXTRA_NEED_MODEL = "EXTRA_NEED_MODEL"
        private const val OTHER = "other"

        //to create instance, use PostDetailsFactory
    }
}