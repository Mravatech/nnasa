package com.mnassa.screen.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.await
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.CommentModel
import com.mnassa.domain.model.RewardModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.extensions.*
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.rewarding.RewardingController
import com.mnassa.screen.posts.need.details.adapter.PostCommentsRVAdapter
import com.mnassa.screen.posts.need.recommend.RecommendController
import com.mnassa.screen.posts.need.recommend.adapter.SelectedAccountRVAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.comment_panels.view.*
import kotlinx.android.synthetic.main.controller_comments_wrapper.view.*
import kotlinx.android.synthetic.main.panel_comment.view.*
import kotlinx.android.synthetic.main.panel_comment_edit.view.*
import kotlinx.android.synthetic.main.panel_recommend.view.*
import kotlinx.android.synthetic.main.panel_reply.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.runBlocking
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/17/2018.
 */
class CommentsWrapperController(args: Bundle) : MnassaControllerImpl<CommentsWrapperViewModel>(args),
        RecommendController.OnRecommendPostResult, MnassaRouter, CommentsWrapperListener, RewardingController.RewardingResult {
    override val layoutId: Int = R.layout.controller_comments_wrapper
    override val viewModel: CommentsWrapperViewModel by instance(fArg = { Pair(wrappedControllerClass, wrappedControllerParams) })
    //
    private val wrappedControllerClass by lazy { args.getSerializable(EXTRA_CONTROLLER_CLASS) as Class<Controller> }
    private val wrappedControllerParams by lazy { args.getBundle(EXTRA_CONTROLLER_ARGS) }
    private val commentsRewardModel by lazy { args.getSerializable(EXTRA_COMMENT_OWNER) as CommentsRewardModel }
    private val wrappedController = StateExecutor<Controller?, CommentsWrapperCallback>(null) { it is CommentsWrapperCallback }
    //
    private val popupMenuHelper: PopupMenuHelper by instance()
    //
    private lateinit var commentsAdapter: PostCommentsRVAdapter
    private val accountsToRecommendAdapter = SelectedAccountRVAdapter()
    private var replyTo: CommentModel? = null
        set(value) {
            field = value
            bindReplyTo(value)
        }
    private var editedComment: CommentModel? = null
        set(value) {
            field = value
            updatePostCommentButtonState()
            bindEditedComment(value)
            value?.let { recommendedAccounts = it.recommends }
        }
    override var recommendedAccounts: List<ShortAccountModel> = emptyList()
    override fun onRecommendedAccountResult(recommendedAccounts: List<ShortAccountModel>) = bindRecommendedAccounts(recommendedAccounts)

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        commentsAdapter = PostCommentsRVAdapter(commentsRewardModel) { inflateHeader(it) }

        commentsAdapter.onReplyClick = { comment -> replyTo = comment }
        commentsAdapter.onCommentOptionsClick = this@CommentsWrapperController::showCommentMenu
        commentsAdapter.onCommentUsefulClick = {
            val controller = RewardingController.newInstance(it.creator, it.id)
            controller.targetController = this@CommentsWrapperController
            open(controller)
        }
        commentsAdapter.onRecommendedAccountClick = { _, profile -> open(ProfileController.newInstance(profile)) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountsToRecommendAdapter.onDataSourceChangedListener = { accounts ->
            recommendedAccounts = accounts
            launchCoroutineUI {
                getCommentsContainer().recommendPanel?.isGone = accounts.isEmpty()
                updatePostCommentButtonState()
            }
        }

        with(view) {
            rvContent.adapter = commentsAdapter
            initializeContainer()
            bindToolbar(toolbar)
        }

        launchCoroutineUI {
            viewModel.canReadCommentsChannel.consumeEach { bindCanReadComments(it) }
        }

        launchCoroutineUI {
            viewModel.canWriteCommentsChannel.consumeEach { bindCanWriteComments(it) }
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
                view.rvContent.post {
                    val dataIndex = commentsAdapter.dataStorage.indexOfFirst { it.id == scrollToComment.id }
                    if (dataIndex < 0) return@post
                    val holderIndex = commentsAdapter.convertDataIndexToAdapterPosition(dataIndex)
                    if (holderIndex < 0) return@post
                    view.rvContent?.smoothScrollToPosition(holderIndex)
                }
            }
        }
    }

    override fun onApplyReward(rewardModel: RewardModel) {
        viewModel.sendPointsForComment(rewardModel)
    }

    private fun initializeContainer() {
        launchCoroutineUI {
            val container = getCommentsContainer()
            container.removeAllViews()

            val inflater = LayoutInflater.from(container.context)
            inflater.inflate(R.layout.comment_panels, container, true)
            inflater.inflate(R.layout.panel_comment, container, true)

            with(container) {
                rvAccountsToRecommend.adapter = accountsToRecommendAdapter

                btnCommentPost.setOnClickListener { onPostCommentClick() }
                ivCommentRecommend.setOnClickListener { openRecommendScreen() }
                updatePostCommentButtonState()

                etCommentText.hint = fromDictionary(R.string.posts_comment_placeholder)
                etCommentText.addTextChangedListener(SimpleTextWatcher { updatePostCommentButtonState() })
                ivReplyCancel.setOnClickListener { replyTo = null }

                editPanel.tvEditTitle.text = fromDictionary(R.string.posts_comment_edit_title)
                editPanel.ivEditCancel.setOnClickListener { editedComment = null }
            }

            bindEditedComment(editedComment)
            bindReplyTo(replyTo)
            bindRecommendedAccounts(recommendedAccounts)
        }
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        runBlocking {
            outState.putString(EXTRA_COMMENT_TEXT, getCommentsContainer().etCommentText.text.toString())
        }
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        launchCoroutineUI {
            with(getCommentsContainer()) {
                etCommentText.setText(savedViewState.getString(EXTRA_COMMENT_TEXT, null))
                etCommentText.setSelection(etCommentText.text.length)
            }
        }
    }

    override fun onDestroyView(view: View) {
        accountsToRecommendAdapter.destroyCallbacks()
        wrappedController.clear()
        view.rvContent.adapter = null
        view.rvAccountsToRecommend.adapter = null

        super.onDestroyView(view)
    }

    override fun openKeyboardOnComment() {
        launchCoroutineUI {
            getCommentsContainer().etCommentText?.apply { showKeyboard(this) }
        }
    }

    private fun inflateHeader(parent: ViewGroup): View {
        val container = LayoutInflater.from(parent.context).inflate(R.layout.recycler_header_container, parent, false)
        container as ViewGroup

        val router = getChildRouter(container)

        if (!router.hasRootController()) {
            val controller = createControllerInstance()
            router.setRoot(RouterTransaction.with(controller))
            wrappedController.value = controller
            //controller needs to be retained. Otherwise controller will be detached right after creation.
            controller.retainViewMode = RetainViewMode.RETAIN_DETACH
        } else {
            val controller = router.backstack.first().controller()
            router.rebindIfNeeded()
            wrappedController.value = controller
        }

        return container
    }

    private fun createControllerInstance(): Controller {
        val emptyParamConstructor = wrappedControllerClass.constructors.firstOrNull { it.parameterTypes.isEmpty() }
        val oneParamConstructor = wrappedControllerClass.constructors.firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == Bundle::class.java }

        return (if (emptyParamConstructor != null) emptyParamConstructor.newInstance() else requireNotNull(oneParamConstructor).newInstance(wrappedControllerParams)) as Controller
    }

    private fun onPostCommentClick() {
        with(view ?: return) {
            val editedCommentLocal = editedComment
            if (editedCommentLocal != null) {
                viewModel.editComment(
                        originalComment = editedCommentLocal,
                        text = etCommentText.text.toString(),
                        accountsToRecommend = recommendedAccounts.map { it.id },
                        replyTo = replyTo)
            } else viewModel.createComment(
                    text = etCommentText.text.toString(),
                    accountsToRecommend = recommendedAccounts.map { it.id },
                    replyTo = replyTo)
            etCommentText.text = null
            replyTo = null
            editedComment = null
            bindRecommendedAccounts(emptyList())
        }
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
        this.recommendedAccounts = value
        accountsToRecommendAdapter.set(value)

        launchCoroutineUI {
            getCommentsContainer().recommendPanel?.isGone = value.isEmpty()
        }
    }

    private fun bindReplyTo(value: CommentModel?) {
        launchCoroutineUI {
            with(getCommentsContainer()) {
                replyPanel.isGone = value == null
                if (value == null) return@launchCoroutineUI
                replyPanel.tvReplyTo.text = fromDictionary(R.string.posts_comment_reply_to)
                replyPanel.tvReplyTo.append(" ")
                replyPanel.tvReplyTo.append(value.creator.formattedName)
            }
        }
    }

    private fun bindCanReadComments(canReadComments: Boolean) {
        if (!canReadComments) {
            commentsAdapter.isLoadingEnabled = false
            commentsAdapter.clear()
        }
        wrappedController.invoke { it.bindCanReadComments(canReadComments) }
    }

    private fun bindCanWriteComments(canWriteComments: Boolean) {
        view?.commentPanel?.isGone = !canWriteComments
        wrappedController.invoke { it.bindCanWriteComments(canWriteComments) }
    }

    private fun bindToolbar(toolbar: MnassaToolbar) {
        wrappedController.invoke {
            it.bindToolbar(toolbar)
        }
    }

    private fun openRecommendScreen() {
        wrappedController.invoke { it.openRecommendScreen(recommendedAccounts.map { it.id }, this) }
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

    private suspend fun getCommentsContainer(): ViewGroup {
        val wrappedController = wrappedController.await()
        return wrappedController.getCommentInputContainer(this@CommentsWrapperController)
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(this, controller)
    override fun close(self: Controller) = mnassaRouter.close(self)

    companion object {
        private const val EXTRA_CONTROLLER_CLASS = "EXTRA_CONTROLLER_CLASS"
        private const val EXTRA_CONTROLLER_ARGS = "EXTRA_CONTROLLER_ARGS"
        private const val EXTRA_COMMENT_TEXT = "EXTRA_COMMENT_TEXT"
        private const val EXTRA_COMMENT_OWNER = "EXTRA_POST_OWNER"

        fun newInstance(controllerToWrap: Controller, commentsRewardModel: CommentsRewardModel): CommentsWrapperController {
            val args = Bundle()
            args.putSerializable(EXTRA_CONTROLLER_CLASS, controllerToWrap.javaClass)
            args.putBundle(EXTRA_CONTROLLER_ARGS, controllerToWrap.args)
            args.putSerializable(EXTRA_COMMENT_OWNER, commentsRewardModel)
            return CommentsWrapperController(args)
        }
    }

    interface CommentsWrapperCallback : CommentInputContainer {
        fun bindToolbar(toolbar: MnassaToolbar) {
            toolbar.isGone = true
        }

        fun openRecommendScreen(recommendedAccountIds: List<String>, self: CommentsWrapperController) {
            self.open(RecommendController.newInstance(
                    bestMatchesAccounts = emptyList(),
                    selectedAccounts = recommendedAccountIds,
                    excludedAccounts = emptyList(),
                    listener = self
            ))
        }

        fun bindCanReadComments(canReadComments: Boolean) = Unit

        fun bindCanWriteComments(canWriteComments: Boolean) = Unit
    }

    interface CommentInputContainer {
        suspend fun getCommentInputContainer(self: CommentsWrapperController): ViewGroup = self.getViewSuspend().commentPanel
    }
}