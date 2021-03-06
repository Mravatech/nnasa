package com.mnassa.screen.posts.need.details

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.comments.CommentsWrapperController
import com.mnassa.screen.comments.CommentsWrapperListener
import com.mnassa.screen.complaintother.ComplaintOtherController
import com.mnassa.screen.photopager.PhotoPagerController
import com.mnassa.screen.posts.PostDetailsFactory.Companion.EXTRA_POST_AUTHOR_ID
import com.mnassa.screen.posts.PostDetailsFactory.Companion.EXTRA_POST_ID
import com.mnassa.screen.posts.PostDetailsFactory.Companion.EXTRA_POST_MODEL
import com.mnassa.screen.posts.PostsController
import com.mnassa.screen.posts.need.create.CreateNeedController
import com.mnassa.screen.posts.need.details.adapter.PostAttachmentsAdapter
import com.mnassa.screen.posts.need.details.adapter.PostTagRVAdapter
import com.mnassa.screen.posts.need.recommend.RecommendController
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import com.mnassa.widget.MnassaToolbar
import kotlinx.android.synthetic.main.controller_need_details.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/19/2018.
 */
open class NeedDetailsController(args: Bundle) : MnassaControllerImpl<NeedDetailsViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult,
        ComplaintOtherController.OnComplaintResult,
        CommentsWrapperController.CommentsWrapperCallback,
        RecommendController.OnRecommendPostResult {
    override val layoutId: Int = R.layout.controller_need_details
    protected val postId by lazy { requireNotNull(args.getString(EXTRA_POST_ID)) }
    protected var post: PostModel? = null
    override val viewModel: NeedDetailsViewModel by instance(arg = getParams(args))
    override var sharingOptions = PostPrivacyOptions.DEFAULT
        set(value) {
            GlobalScope.launchUI {
                getViewSuspend()
                viewModel.repost(value)
            }
            field = value
        }
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val dialogHelper: DialogHelper by instance()
    private val tagsAdapter = PostTagRVAdapter()
    override var onComplaint: String = ""
        set(value) {
            viewModel.sendComplaint(postId, OTHER, value)
        }
    private val commentsWrapper by lazy { parentController as CommentsWrapperListener }

    val I_OFFER = "I-OFFER"

    //To get an instance of Shared Preference
    private var offerSharedPreference: SharedPreferences? = null
    private var offerSharedPreferenceEditor: SharedPreferences.Editor? = null
    private var offerSharedPreferenceContent: MutableSet<String>? = mutableSetOf("")


    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        offerSharedPreference = applicationContext?.getSharedPreferences("Offer-Shared-Prefs", Context.MODE_PRIVATE)
        offerSharedPreferenceEditor = offerSharedPreference?.edit()

        offerSharedPreferenceContent = offerSharedPreference?.getStringSet(I_OFFER, mutableSetOf(""))

        with(view) {
            rvTags.layoutManager = ChipsLayoutManager.newBuilder(context)
                    .setScrollingEnabled(false)
                    .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            rvTags.adapter = tagsAdapter
        }

        launchCoroutineUI {
            viewModel.postChannel.consumeEach {
                bindPost(it, getViewSuspend())
            }
        }

        launchCoroutineUI { viewModel.postTagsChannel.consumeEach { bindTags(it, getViewSuspend()) } }

        launchCoroutineUI { viewModel.finishScreenChannel.consumeEach { close() } }

        (args.getSerializable(EXTRA_POST_MODEL) as PostModel?)?.let { post ->
            bindPost(post, view)
            args.remove(EXTRA_POST_MODEL)
        }
    }

    override fun onDestroyView(view: View) {
        view.rvTags.adapter = null
        super.onDestroyView(view)
    }

    override fun onRecommendedAccountResult(recommendedAccounts: List<ShortAccountModel>) = commentsWrapper.onRecommendedAccountResult(recommendedAccounts)

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

    protected open suspend fun showMyPostMenu(view: View, post: PostModel) {
        popupMenuHelper.showMyPostMenu(
                view = view,
                post = post,
                onEditPost = { open(CreateNeedController.newInstanceEditMode(post)) },
                onDeletePost = { viewModel.delete() },
                onPromotePost = { viewModel.promote() },
                changeStatus = { viewModel.changeStatus(it) }
        )
    }

    protected open suspend fun showOtherUserPostMenu(view: View, post: PostModel) {
        popupMenuHelper.showPostMenu(
                view = view,
                post = post,
                onRepost = { openSharingOptionsScreen() },
                onReport = { complainAboutProfile(view) }
        )
    }

    protected open fun bindPost(post: PostModel, view: View) {
        this.post = post

        with(view) {
            Log.d("postDebug", post.toString());
            //author block
            ivAvatar.avatarRound(post.author.avatar)
            tvUserName.text = post.author.formattedName
            tvPosition.text = post.author.formattedPosition
            tvPosition.goneIfEmpty()
            tvEventName.text = post.author.formattedFromEvent
            tvEventName.goneIfEmpty()

            if (offerSharedPreferenceContent != null) {

                if (offerSharedPreferenceContent!!.contains(postId)) {


                    ivChat.text = applicationContext?.resources?.getString(R.string.my_offer)

                } else {
                    ivChat.text = applicationContext?.resources?.getString(R.string.i_offer)

                }
            }




            ivChat.setOnClickListener {
                offerSharedPreferenceEditor?.putStringSet(I_OFFER, mutableSetOf(postId))
                offerSharedPreferenceEditor?.commit()
                open(ChatMessageController.newInstance(post, post.author))
            }
            rlCreatorRoot.setOnClickListener { open(ProfileController.newInstance(post.author)) }

            //
            tvNeedDescription.text = post.formattedText
            tvNeedDescription.goneIfEmpty()
            //attachments
            flImages.isGone = post.attachments.isEmpty()
            if (post.attachments.isNotEmpty()) {
                pivImages.count = post.attachments.size
                pivImages.selection = 0

                vpImages.adapter = PostAttachmentsAdapter(this@NeedDetailsController, post.attachments) { images, index ->
                    open(PhotoPagerController.newInstance(images, index))
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
            tvCreationTime.text = post.createdAt.formatAsDateTime()

            //views count
            tvViewsCount.text = fromDictionary(R.string.need_views_count).format(post.counters.views)
            ivRepost.setOnClickListener { openSharingOptionsScreen() }
            ivRepost.text = fromDictionary(R.string.need_repost_count).format(post.counters.reposts)
            ivComment.text = fromDictionary(R.string.need_comments_count_new).format(post.counters.comments)
            ivRepost.isInvisible = false
            tvRepostsCount.visibility = ivRepost.visibility

//            ivRepost.isInvisible = !post.canBeShared

            //expiration
            tvExpiration.bindExpireType(post.statusOfExpiration, post.timeOfExpiration)

            btnFufiflAct.bindText(post.statusOfExpiration, post.timeOfExpiration, false)

            myText.bindStatus(post.statusOfExpiration, post.timeOfExpiration, "heading")

            longText.bindStatus(post.statusOfExpiration, post.timeOfExpiration, "body")

            btnComment.text = fromDictionary(R.string.need_comment_button)
            ivComment.setOnClickListener { commentsWrapper.openKeyboardOnComment() }

            val recommendWithCount = StringBuilder(fromDictionary(R.string.need_fine_someone_button))
            if (post.autoSuggest.total > 0) {
                recommendWithCount.append(" (")
                recommendWithCount.append(post.autoSuggest.total.toString())
                recommendWithCount.append(") ")
            }

            btnRecommend.text = recommendWithCount
            btnRecommend.setOnClickListener { openRecommendScreen(post, commentsWrapper.accountsToRecommend.map { it.id }) }

            btnFufiflAct.setOnClickListener {
                viewModel.changeStatus(ExpirationType.FULFILLED)
                btnFufiflAct.visibility = View.GONE
            }

            tvCommentsCount.setHeaderWithCounter(R.string.need_comments_count, post.counters.comments)

            //show button
            ivChat.visibility = if (!post.isMyPost()) View.VISIBLE else View.GONE
            btnFufiflAct.visibility = if (post.isMyPost()) View.VISIBLE else View.GONE
            odour.visibility = if (post.isMyPost()) View.VISIBLE else View.GONE
            odour1.visibility = if (post.isMyPost()) View.VISIBLE else View.GONE



            llOtherPersonPostActions.bindStat(post.statusOfExpiration, post.timeOfExpiration, false)


            val parentController = parentController
            if (parentController is CommentsWrapperController) {
                launchCoroutineUI {
                    parentController.bindCanRecommend(post.canRecommend)
                }
            }
        }
    }

    protected open fun bindTags(tags: List<TagModel>, view: View) {
        with(view) {
            vTagsSeparator.isGone = tags.isEmpty()
            rvTags.isGone = tags.isEmpty()
            tagsAdapter.set(tags)
        }
    }

    override fun bindToolbar(toolbar: MnassaToolbar) {
        launchCoroutineUI {
            val post = viewModel.postChannel.consume { receive() }
            toolbar.title = fromDictionary(R.string.need_details_title).format(post.author.formattedName)
            if (post.isMyPost()) {
                toolbar.onMoreClickListener = onClick { view ->
                    val post = viewModel.postChannel.consumeOne()
                    showMyPostMenu(view, post)
                }
                makePostActionsGone()
            } else {
                toolbar.onMoreClickListener = onClick { view ->
                    val post = viewModel.postChannel.consumeOne()
                    showOtherUserPostMenu(view, post)
                }
                if (post.canRecommend) makePostActionsVisible()
                else makePostActionsGone()
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
            //            it.llOtherPersonPostActions.isGone = true
            it.vOtherPersonPostActionsSeparator.isGone = true
        }
    }

    protected open suspend fun makePostActionsVisible() {
        getViewSuspend().let {
            //            it.llOtherPersonPostActions.visibility = View.VISIBLE
            it.vOtherPersonPostActionsSeparator.visibility = View.VISIBLE
        }
    }

    private fun openSharingOptionsScreen() {
        post?.takeIf { it.canBeShared }?.let {
            open(SharingOptionsController.newInstance(
                    listener = this,
                    accountsToExclude = listOf(it.author.id),
                    restrictShareReduction = false,
                    canBePromoted = false,
                    promotePrice = 0L))
        }
    }

    companion object {

        private const val OTHER = "other"

        //to create instance, use PostDetailsFactory

        fun getParams(args: Bundle): NeedDetailsViewModel.ViewModelParams {
            return NeedDetailsViewModel.ViewModelParams(
                    postId = args.getString(EXTRA_POST_ID),
                    postAuthorId = args.getString(EXTRA_POST_AUTHOR_ID),
                    post = args.getSerializable(EXTRA_POST_MODEL) as PostModel?
            )
        }
    }
}