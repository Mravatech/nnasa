package com.mnassa.screen.comments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.needAttach
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.activity.PhotoPagerActivity
import com.mnassa.core.addons.StateExecutor
import com.mnassa.core.addons.await
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.comments.rewarding.RewardingController
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.details.adapter.PostCommentsRVAdapter
import com.mnassa.screen.posts.need.recommend.RecommendController
import com.mnassa.screen.posts.need.recommend.adapter.SelectedAccountRVAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.comment_panels.view.*
import kotlinx.android.synthetic.main.controller_comments_wrapper.view.*
import kotlinx.android.synthetic.main.panel_comment.view.*
import kotlinx.android.synthetic.main.panel_comment_attachments.view.*
import kotlinx.android.synthetic.main.panel_comment_edit.view.*
import kotlinx.android.synthetic.main.panel_recommend.view.*
import kotlinx.android.synthetic.main.panel_reply.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.runBlocking
import org.kodein.di.generic.instance
import timber.log.Timber

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
    private val initializedContainer = StateExecutor<Unit?, Unit>(null) { it != null }
    //
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val dialogHelper: DialogHelper by instance()
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
            bindEditedComment(value, true)
            value?.let {
                bindRecommendedAccounts(it.recommends)
                bindCommentAttachments(it.images.map(AttachedImage::UploadedImage))
            }
        }
    private val attachmentsAdapter = CommentAttachmentsAdapter()

    override fun onRecommendedAccountResult(recommendedAccounts: List<ShortAccountModel>) = bindRecommendedAccounts(recommendedAccounts)
    override val accountsToRecommend: List<ShortAccountModel> get() = accountsToRecommendAdapter.dataStorage.toList()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        commentsAdapter = PostCommentsRVAdapter(commentsRewardModel, headerInflater = { inflateHeader(it) }, reverseOrder = false)
        commentsAdapter.isLoadingEnabled = true

        commentsAdapter.onReplyClick = { comment -> replyTo = comment }
        commentsAdapter.onCommentOptionsClick = this@CommentsWrapperController::showCommentMenu
        commentsAdapter.onCommentUsefulClick = {
            open(RewardingController.newInstance(this@CommentsWrapperController, it.creator, it.id))
        }
        commentsAdapter.onRecommendedAccountClick = { _, profile -> open(ProfileController.newInstance(profile)) }
        commentsAdapter.onCommentAuthorClick = { open(ProfileController.newInstance(it)) }
        commentsAdapter.onImageClickListener = { comment, position ->
            view?.context?.let { context ->
                PhotoPagerActivity.start(
                        context = context,
                        images = comment.images,
                        selectedItem = position
                )
            }
        }

        accountsToRecommendAdapter.onDataChangedListener = { itemsCount ->
            launchCoroutineUI {

                getCommentsContainer().recommendPanel?.isGone = itemsCount == 0
                updatePostCommentButtonState()
            }
        }

        attachmentsAdapter.onDataChangedListener = { itemsCount ->
            launchCoroutineUI {
                getCommentsContainer().attachmentsPanel?.isGone = itemsCount == 0
                updatePostCommentButtonState()
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvContent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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

    override fun onViewDestroyed(view: View) {
        initializedContainer.value = null
        super.onViewDestroyed(view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_CROP) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri? = data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                uri?.let {
                    attachmentsAdapter.dataStorage.add(AttachedImage.LocalImage(it))
                    viewModel.preloadImage(it)
                }
            }
            CropActivity.GET_PHOTO_ERROR -> {
                Timber.e("CropActivity.GET_PHOTO_ERROR")
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
                rvCommentAttachments.adapter = attachmentsAdapter

                btnCommentPost.setOnClickListener { onPostCommentClick() }
                ivCommentRecommend.setOnClickListener { openRecommendScreen() }
                ivCommentAttach.setOnClickListener {
                    dialogHelper.showSelectImageSourceDialog(it.context) { imageSource -> launchCoroutineUI { selectImage(imageSource) } }
                }
                updatePostCommentButtonState()

                etCommentText.hint = fromDictionary(R.string.posts_comment_placeholder)
                etCommentText.addTextChangedListener(SimpleTextWatcher { updatePostCommentButtonState() })
                ivReplyCancel.setOnClickListener { replyTo = null }

                editPanel.tvEditTitle.text = fromDictionary(R.string.posts_comment_edit_title)
                editPanel.ivEditCancel.setOnClickListener { editedComment = null }
            }

            bindEditedComment(editedComment, false)
            bindReplyTo(replyTo)
            container.recommendPanel?.isGone = accountsToRecommendAdapter.dataStorage.isEmpty()
            container.attachmentsPanel?.isGone = attachmentsAdapter.dataStorage.isEmpty()
            initializedContainer.value = Unit
        }
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        getCommentsContainerNullable()?.let { container ->
            outState.putString(EXTRA_COMMENT_TEXT, container.etCommentText.text.toString())
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
        wrappedController.clear()
        view.rvContent.adapter = null
        view.rvAccountsToRecommend.adapter = null

        getCommentsContainerNullable()?.let { container ->
            with(container) {
                rvAccountsToRecommend?.adapter = null
                rvCommentAttachments?.adapter = null
            }
        }

        super.onDestroyView(view)
    }

    override fun openKeyboardOnComment() {
        getCommentsContainerNullable()?.etCommentText?.apply { showKeyboard(this) }
    }

    suspend fun bindCanRecommend(canRecommend: Boolean) {
        //
        initializedContainer.await()
        getCommentsContainer().ivCommentRecommend?.isGone = !canRecommend
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
            controller.needAttach = true
            router.rebindIfNeeded()
            wrappedController.value = controller
        }

        return container
    }

    private fun createControllerInstance(): Controller {
        val emptyParamConstructor = wrappedControllerClass.constructors.firstOrNull { it.parameterTypes.isEmpty() }
        val oneParamConstructor = wrappedControllerClass.constructors.firstOrNull { it.parameterTypes.size == 1 && it.parameterTypes[0] == Bundle::class.java }

        return (if (emptyParamConstructor != null) emptyParamConstructor.newInstance()
        else requireNotNull(oneParamConstructor).newInstance(wrappedControllerParams)) as Controller
    }

    private fun onPostCommentClick() {
        with(view ?: return) {
            val editedCommentLocal = editedComment
            launchCoroutineUI {
                val result = if (editedCommentLocal != null) {
                    viewModel.editComment(makeCommentModel())
                } else viewModel.createComment(makeCommentModel())
                if (result) {
                    etCommentText.text = null
                    replyTo = null
                    editedComment = null
                    bindRecommendedAccounts(emptyList())
                    bindCommentAttachments(emptyList())
                }
            }
        }
    }

    private fun makeCommentModel(): RawCommentModel {
        return with(requireNotNull(view)) {
            RawCommentModel(
                    id = editedComment?.id,
                    parentCommentId = replyTo?.id ?: editedComment?.parentCommentId,
                    text = etCommentText.text.toString().takeIf { it.isNotBlank() },
                    accountsToRecommend = accountsToRecommend.map { it.id },
                    uploadedImages = attachmentsAdapter.dataStorage.filterIsInstance(AttachedImage.UploadedImage::class.java).map { it.imageUrl },
                    imagesToUpload = attachmentsAdapter.dataStorage.filterIsInstance(AttachedImage.LocalImage::class.java).map { it.imageUri },
                    postId = wrappedControllerParams.getString(PostDetailsFactory.EXTRA_POST_ID, null)
                            ?: wrappedControllerParams.getString(EventDetailsController.EXTRA_EVENT_ID)
            )
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

    private fun bindEditedComment(value: CommentModel?, setText: Boolean) {
        launchCoroutineUI {
            with(getViewSuspend()) {
                editPanel.isGone = value == null
                if (value == null) {
                    hideKeyboard(commentPanel.etCommentText)
                    return@launchCoroutineUI
                }

                editPanel.tvEditText.text = value.text
                editPanel.tvEditText.goneIfEmpty()
                if (setText) {
                    commentPanel.etCommentText.setText(value.text)
                    commentPanel.etCommentText.setSelection(value.text?.length ?: 0)
                }
                showKeyboard(commentPanel.etCommentText)
            }
        }
    }

    private fun bindRecommendedAccounts(value: List<ShortAccountModel>) {
        accountsToRecommendAdapter.set(value)

        launchCoroutineUI {
            getCommentsContainer().recommendPanel?.isGone = value.isEmpty()
        }
    }

    private fun bindCommentAttachments(value: List<AttachedImage>) {
        attachmentsAdapter.set(value)

        launchCoroutineUI {
            getCommentsContainer().attachmentsPanel?.isGone = value.isEmpty()
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
        view?.commentPanelShadow?.isGone = !canWriteComments
        wrappedController.invoke { it.bindCanWriteComments(canWriteComments) }
    }

    private fun bindToolbar(toolbar: MnassaToolbar) {
        wrappedController.invoke {
            it.bindToolbar(toolbar)
        }
    }

    private fun openRecommendScreen() {
        wrappedController.invoke { it.openRecommendScreen(accountsToRecommend.map { it.id }, this) }
    }

    private val canPostComment: Boolean
        get() = (view?.etCommentText?.text?.isNotBlank() ?: false)
                || !accountsToRecommendAdapter.dataStorage.isEmpty()
                || !attachmentsAdapter.dataStorage.isEmpty()

    private fun updatePostCommentButtonState() {
        val text = when {
            editedComment != null -> fromDictionary(R.string.posts_comment_edit_button)
            else -> fromDictionary(R.string.posts_comment_create)
        }

        with(view ?: return) {
            btnCommentPost.text = text
            btnCommentPost.isEnabled = canPostComment

            ivCommentAttach.isGone = attachmentsAdapter.dataStorage.size >= MAX_ATTACHED_IMAGES_COUNT
        }
    }

    private suspend fun getCommentsContainer(): ViewGroup {
        val wrappedController = wrappedController.await()
        return wrappedController.getCommentInputContainer(this@CommentsWrapperController)
    }

    private fun getCommentsContainerNullable(): ViewGroup? {
        val wrappedController = wrappedController.value as? CommentsWrapperCallback?
        return wrappedController?.getCommentInputContainerNullable(this@CommentsWrapperController)
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        val permissionsList = when (imageSource) {
            CropActivity.ImageSource.GALLERY -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            CropActivity.ImageSource.CAMERA -> listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionResult = permissions.requestPermissions(permissionsList)
        if (permissionResult.isAllGranted) {
            activity?.let {
                val intent = CropActivity.start(imageSource, it)
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }
        }
    }

    override fun open(self: Controller, controller: Controller) = mnassaRouter.open(this, controller)
    override fun close(self: Controller) = mnassaRouter.close(self)

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val MAX_ATTACHED_IMAGES_COUNT = 5

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
        fun getCommentInputContainerNullable(self: CommentsWrapperController): ViewGroup? = self.view?.commentPanel
    }
}