package com.mnassa.screen.posts.need.details

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.Toast
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.mnassa.R
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.adapter.PhotoPagerAdapter
import com.mnassa.screen.posts.need.details.adapter.PostCommentsRVAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.screen.posts.need.recommend.RecommendController
import com.mnassa.screen.posts.need.recommend.adapter.SelectedAccountRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_need_details_header.view.*
import kotlinx.android.synthetic.main.controller_post_details.view.*
import kotlinx.android.synthetic.main.panel_comment.view.*
import kotlinx.android.synthetic.main.panel_comment_edit.view.*
import kotlinx.android.synthetic.main.panel_recommend.view.*
import kotlinx.android.synthetic.main.panel_reply.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 3/19/2018.
 */
class PostDetailsController(args: Bundle) : MnassaControllerImpl<PostDetailsViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult,
        RecommendController.OnRecommendPostResult {
    override val layoutId: Int = R.layout.controller_post_details
    private val postId by lazy { args.getString(EXTRA_NEED_ID) }
    override val viewModel: PostDetailsViewModel by injector.with(postId).instance()
    override var sharingOptions: SharingOptionsController.ShareToOptions = SharingOptionsController.ShareToOptions.EMPTY
        set(value) = viewModel.repost(value)
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val tagsAdapter = PostTagRVAdapter()
    private val commentsAdapter = PostCommentsRVAdapter()
    private val accountsToRecommendAdapter = SelectedAccountRVAdapter()
    private var headerLayout = StateExecutor<View?, View>(null) { it != null }
    private var replyTo: CommentModel? = null
        set(value) {
            field = value
            launchCoroutineUI {
                with(getViewSuspend()) {
                    replyPanel.isGone = value == null
                    if (value == null) return@launchCoroutineUI
                    replyPanel.tvReplyTo.text = fromDictionary(R.string.posts_comment_reply_to)
                    replyPanel.tvReplyTo.append(" ")
                    replyPanel.tvReplyTo.append(value.creator.formattedName)
                }
            }
        }
    private var editedComment: CommentModel? = null
        set(value) {
            field = value
            updatePostCommentButtonState()
            bindEditedComment(value)
            value?.let { recommendedAccounts = it.recommends }
        }
    override var recommendedAccounts: List<ShortAccountModel>
        set(value) = bindRecommendedAccounts(value)
        get() = accountsToRecommendAdapter.dataStorage.toList()

    private fun bindEditedComment(value: CommentModel?) {
        launchCoroutineUI {
            with(getViewSuspend()) {
                editPanel.isGone = value == null
                if (value == null) {
                    hideKeyboard(commentPanel.etCommentText)
                    return@launchCoroutineUI
                }

                editPanel.tvEditText.text = value.text
                editPanel.tvEditText.goneIfEmpty()
                commentPanel.etCommentText.setText(value.text)
                commentPanel.etCommentText.setSelection(value.text?.length ?: 0)
                showKeyboard(commentPanel.etCommentText)
            }
        }
    }

    private fun bindRecommendedAccounts(value: List<ShortAccountModel>) {
        launchCoroutineUI {
            getViewSuspend().recommendPanel.isGone = value.isEmpty()
            accountsToRecommendAdapter.set(value)
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            commentsAdapter.onBindHeader = { headerLayout.value = it }
            commentsAdapter.onReplyClick = { comment -> replyTo = comment }
            commentsAdapter.onCommentOptionsClick = this@PostDetailsController::showCommentMenu
            rvPostDetails.adapter = commentsAdapter
            rvAccountsToRecommend.adapter = accountsToRecommendAdapter
            accountsToRecommendAdapter.onDataSourceChangedListener = {
                recommendPanel.isGone = it.isEmpty()
                updatePostCommentButtonState()
            }

            btnCommentPost.setOnClickListener {
                val editedCommentLocal = editedComment
                if (editedCommentLocal != null) {
                    viewModel.editComment(editedCommentLocal, etCommentText.text.toString(), recommendedAccounts.map { it.id }, replyTo)
                } else viewModel.createComment(etCommentText.text.toString(), recommendedAccounts.map { it.id }, replyTo)
                etCommentText.text = null
                replyTo = null
                editedComment = null
                recommendedAccounts = emptyList()
            }
            updatePostCommentButtonState()

            etCommentText.hint = fromDictionary(R.string.posts_comment_placeholder)
            etCommentText.addTextChangedListener(SimpleTextWatcher { updatePostCommentButtonState() })
            tvCommentRecommend.setOnClickListener { headerLayout.invoke { it.btnRecommend.performClick() } }
            ivReplyCancel.setOnClickListener { replyTo = null }

            editPanel.tvEditTitle.text = fromDictionary(R.string.posts_comment_edit_title)
            editPanel.ivEditCancel.setOnClickListener { editedComment = null }
        }

        headerLayout.invoke {
            with(it) {
                rvTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                        .setScrollingEnabled(false)
                        .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                        .setOrientation(ChipsLayoutManager.HORIZONTAL)
                        .build()
                rvTags.adapter = tagsAdapter
            }
        }

        launchCoroutineUI { viewModel.postChannel.consumeEach { setPost(it) } }

        launchCoroutineUI { viewModel.postTagsChannel.consumeEach { setTags(it) } }

        launchCoroutineUI { viewModel.finishScreenChannel.consumeEach { close() } }

        launchCoroutineUI {
            viewModel.canReadCommentsChannel.consumeEach { canReadComments ->
                if (!canReadComments) {
                    commentsAdapter.isLoadingEnabled = false
                    commentsAdapter.clear()
                }
                headerLayout.invoke {
                    it.tvCommentsCount.isGone = !canReadComments
                }
            }
        }

        launchCoroutineUI {
            viewModel.canWriteCommentsChannel.consumeEach { canWriteComments ->
                view.commentPanel.isGone = !canWriteComments
                headerLayout.invoke {
                    it.llOtherPersonPostActions.isGone = !canWriteComments
                    it.vOtherPersonPostActionsSeparator.isGone = !canWriteComments
                }
            }
        }

        commentsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.commentsChannel.consumeEach {
                commentsAdapter.isLoadingEnabled = false
                commentsAdapter.set(it)
            }
        }

        launchCoroutineUI {
            viewModel.scrollToChannel.consumeEach { scrollToComment ->
                view.rvPostDetails.post {
                    val dataIndex = commentsAdapter.dataStorage.indexOfFirst { it.id == scrollToComment.id }
                    if (dataIndex < 0) return@post
                    val holderIndex = commentsAdapter.convertDataIndexToAdapterPosition(dataIndex)
                    if (holderIndex < 0) return@post
                    view.rvPostDetails?.smoothScrollToPosition(holderIndex)
                }
            }
        }

        (args.getSerializable(EXTRA_NEED_MODEL) as PostModel?)?.apply {
            setPost(this)
            args.remove(EXTRA_NEED_MODEL)
        }

        bindEditedComment(editedComment)
    }

    override fun onDestroyView(view: View) {
        headerLayout.value = null
        headerLayout.clear()
        commentsAdapter.destroyCallbacks()
        accountsToRecommendAdapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    private fun showPostMenu(view: View) {
        popupMenuHelper.showPostMenu(
                view = view,
                onRepost = { openSharingOptionsScreen() },
                onReport = { Toast.makeText(view.context, "Report post", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun showCommentMenu(view: View, commentModel: CommentModel) {
        launchCoroutineUI {
            if (!commentModel.isMyComment()) return@launchCoroutineUI

            popupMenuHelper.showMyCommentMenu(
                    view = view,
                    onEditComment = { editedComment = commentModel },
                    onDeleteComment = { viewModel.deleteComment(commentModel) }
            )
        }
    }

    private fun setPost(post: PostModel) {
        Timber.d("POST -> setPost $post")

        view?.toolbar?.title = fromDictionary(R.string.need_details_title).format(post.author.formattedName)

        headerLayout.invoke {
            with(it) {

                //author block
                ivAvatar.avatarRound(post.author.avatar)
                tvUserName.text = post.author.formattedName
                tvPosition.text = post.author.formattedPosition
                tvPosition.goneIfEmpty()
                tvEventName.text = post.author.formattedFromEvent
                tvEventName.goneIfEmpty()
                ivChat.setOnClickListener {
                    Toast.makeText(context, "Opening chat with ${post.author.formattedName}", Toast.LENGTH_SHORT).show()
                }

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
                btnComment.setOnClickListener {
                    view?.etCommentText?.apply { showKeyboard(this) }
                }

                val recommendWithCount = StringBuilder(fromDictionary(R.string.need_recommend_button))
                if (post.autoSuggest.total > 0) {
                    recommendWithCount.append(" (")
                    recommendWithCount.append(post.autoSuggest.total.toString())
                    recommendWithCount.append(") ")
                }

                btnRecommend.text = recommendWithCount
                btnRecommend.setOnClickListener { openRecommendScreen(post) }
                tvCommentsCount.setHeaderWithCounter(R.string.need_comments_count, post.counters.comments)
            }
        }

        launchCoroutineUI {
            val view = view ?: return@launchCoroutineUI
            if (post.isMyPost()) {
                view.toolbar.onMoreClickListener = {
                    popupMenuHelper.showMyPostMenu(
                            view = it,
                            onEditPost = { open(CreateNeedController.newInstanceEditMode(post)) },
                            onDeletePost = { viewModel.delete() })
                }
                headerLayout.invoke {
                    it.llOtherPersonPostActions.visibility = View.GONE
                }
            } else {
                view.toolbar.onMoreClickListener = { showPostMenu(it) }
                headerLayout.invoke {
                    it.llOtherPersonPostActions.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setTags(tags: List<TagModel>) {
        headerLayout.invoke {
            with(it) {
                vTagsSeparator.isGone = tags.isEmpty()
                rvTags.isGone = tags.isEmpty()
                tagsAdapter.set(tags)
            }
        }
    }

    private fun openSharingOptionsScreen() {
        val controller = SharingOptionsController.newInstance()
        controller.targetController = this
        open(controller)
    }

    private fun openRecommendScreen(post: PostModel) {
        val controller = RecommendController.newInstance(post.author.formattedName, post.autoSuggest.accountIds, recommendedAccounts.map { it.id })
        controller.targetController = this
        open(controller)
    }

    private val canPostComment: Boolean
        get() = (view?.etCommentText?.text?.isNotBlank()
                ?: false) || accountsToRecommendAdapter.dataStorage.size > 0

    private fun updatePostCommentButtonState() {
        val text = when {
            editedComment != null -> fromDictionary(R.string.posts_comment_edit_button)
            else -> fromDictionary(R.string.posts_comment_create)
        }
        view?.btnCommentPost?.let {
            it.text = text
            it.isEnabled = canPostComment
        }
    }

    companion object {
        private const val EXTRA_NEED_ID = "EXTRA_NEED_ID"
        private const val EXTRA_NEED_MODEL = "EXTRA_NEED_MODEL"

        fun newInstance(postId: String): PostDetailsController {
            val args = Bundle()
            args.putString(EXTRA_NEED_ID, postId)
            return PostDetailsController(args)
        }

        fun newInstance(post: PostModel): PostDetailsController {
            val args = Bundle()
            args.putString(EXTRA_NEED_ID, post.id)
            args.putSerializable(EXTRA_NEED_MODEL, post)
            return PostDetailsController(args)
        }
    }
}