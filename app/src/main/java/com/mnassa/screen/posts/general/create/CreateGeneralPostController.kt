package com.mnassa.screen.posts.general.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.RawPostModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.lengthOrZero
import com.mnassa.extensions.startCropActivityForResult
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.posts.need.sharing.format
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_general_post_create.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 4/30/2018.
 */
class CreateGeneralPostController(args: Bundle) : MnassaControllerImpl<CreateGeneralPostViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_general_post_create
    private val groupIds by lazy { args.getStringArrayList(EXTRA_GROUP_ID) ?: emptyList<String>() }
    private val postId by lazy { args.getString(EXTRA_POST_ID, null) }
    override val viewModel: CreateGeneralPostViewModel by instance(arg = postId)
    override var sharingOptions = getSharingOptions(args)
        set(value) {
            field = value
            applyShareOptionsChanges()
        }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialogHelper: DialogHelper by instance()
    private val attachedImagesAdapter = AttachedImagesRVAdapter()
    private var placeId: String? = null
    private var imageToReplace: AttachedImage? = null
    private var post: PostModel? = null


    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()

        initAttachedImagesAdapter(view)

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.general_publish)) {
                view.toolbar.actionButtonClickable = false
                launchCoroutineUI {
                    viewModel.applyChanges(makePostModel())
                }.invokeOnCompletion { onGeneralTextUpdated() }
            }
            tvShareOptions.setOnClickListener(::openShareOptionsScreen)

            applyShareOptionsChanges()
            etGeneralPost.hint = fromDictionary(R.string.general_text_placeholder)
            etGeneralPost.inputType = etGeneralPost.inputType or EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
            etGeneralPost.addTextChangedListener(SimpleTextWatcher { onGeneralTextUpdated() })
            onGeneralTextUpdated()

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.autodetectTagsFrom(etGeneralPost)

            initPlaceAutoComplete(view)

            rvImages.adapter = attachedImagesAdapter

            if (postId != null) {
                toolbar.title = fromDictionary(R.string.general_edit_title)
            }
        }

        if (args.containsKey(EXTRA_POST)) {
            setData(args.getSerializable(EXTRA_POST) as PostModel)
            args.remove(EXTRA_POST)
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
        }

        if (postId == null && placeId == null) {
            launchCoroutineUI {
                viewModel.getUserLocation()?.let {
                    placeId = it.placeId
                    view.actvPlace.setText(it.placeName.toString())
                }
            }
        }
    }

    private fun openShareOptionsScreen(view: View) {
        if (groupIds.isNotEmpty()) return

        launchCoroutineUI {
            val post = post
            val canBePromoted = viewModel.canPromotePost()
            val promotePrice = viewModel.getPromotePostPrice()
            open(SharingOptionsController.newInstance(
                    options = sharingOptions,
                    listener = this@CreateGeneralPostController,
                    accountsToExclude = if (post != null) listOf(post.author.id) else emptyList(),
                    restrictShareReduction = postId != null,
                    canBePromoted = canBePromoted,
                    promotePrice = promotePrice))
        }
    }

    private fun initAttachedImagesAdapter(view: View) {
        attachedImagesAdapter.onAddImageClickListener = {
            dialogHelper.showSelectImageSourceDialog(view.context) { imageSource -> launchCoroutineUI { selectImage(imageSource) } }
        }
        attachedImagesAdapter.onRemoveImageClickListener = { _, item ->
            attachedImagesAdapter.dataStorage.remove(item)
        }
        attachedImagesAdapter.onReplaceImageClickListener = { _, item ->
            imageToReplace = item
            attachedImagesAdapter.onAddImageClickListener()
        }
    }

    private fun initPlaceAutoComplete(view: View) {
        with(view) {
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${item.secondaryText}"
                actvPlace.setText(placeName)
            }
            tilPlace.hint = fromDictionary(R.string.need_create_city_hint)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_CROP) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri? = data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                uri?.let {
                    val imageToReplaceLocal = imageToReplace
                    val newImage = AttachedImage.LocalImage(uri)
                    if (imageToReplaceLocal != null) {
                        attachedImagesAdapter.replace(imageToReplaceLocal, newImage)
                    } else {
                        attachedImagesAdapter.dataStorage.add(newImage)
                    }
                    imageToReplace = null
                }
            }
            CropActivity.GET_PHOTO_ERROR -> {
                imageToReplace = null
                Timber.e("CropActivity.GET_PHOTO_ERROR")
            }
        }
    }

    override fun onDestroyView(view: View) {
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        attachedImagesAdapter.destroyCallbacks()
        view.rvImages.adapter = null
        super.onDestroyView(view)
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        startCropActivityForResult(imageSource, REQUEST_CODE_CROP)
    }

    private fun setData(post: PostModel) {
        this.post = post
        launchCoroutineUI {
            with(getViewSuspend()) {
                chipTags.cancelAutodetectTagsFrom(etGeneralPost)
                etGeneralPost.setText(post.text)
                chipTags.autodetectTagsFrom(etGeneralPost)
                chipTags.setTags(post.tags.mapNotNull { viewModel.getTag(it) })
                attachedImagesAdapter.set(post.attachments.map { AttachedImage.UploadedImage(it) })

                placeId = post.locationPlace?.placeId
                actvPlace.setText(post.locationPlace?.placeName?.toString())
                sharingOptions = PostPrivacyOptions(
                    post.privacyType,
                    post.privacyConnections,
                    post.groupIds)
                applyShareOptionsChanges()
            }
        }
    }

    private fun onGeneralTextUpdated() {
        view?.toolbar?.actionButtonClickable = canCreatePost()
    }

    private fun canCreatePost(): Boolean {
        val view = view ?: return false
        return view.etGeneralPost.text.lengthOrZero >= MIN_GENERAL_POST_TEXT_LENGTH
    }

    @SuppressLint("SetTextI18n")
    private fun applyShareOptionsChanges() {
        launchCoroutineUI {
            getViewSuspend().tvShareOptions?.text = sharingOptions.format()
        }
    }

    private fun makePostModel(): RawPostModel {
        with(requireNotNull(view)) {
            val images = attachedImagesAdapter.dataStorage.toList()
            return RawPostModel(
                    id = postId,
                    groupIds = groupIds,
                    text = etGeneralPost.text.toString(),
                    tags = chipTags.getTags(),
                    imagesToUpload = images.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri },
                    uploadedImages = images.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl },
                    privacy = sharingOptions,
                    placeId = placeId)
        }
    }


    companion object {
        private const val MIN_GENERAL_POST_TEXT_LENGTH = 3

        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val EXTRA_POST = "EXTRA_POST"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance(group: GroupModel? = null): CreateGeneralPostController {
            val args = Bundle()
            group?.let {
                args.putStringArrayList(EXTRA_GROUP_ID, arrayListOf(it.id))
                args.putSerializable(EXTRA_GROUP, it)
            }
            return CreateGeneralPostController(args)
        }

        fun newInstance(post: PostModel): CreateGeneralPostController {
            val args = Bundle()
            args.putString(EXTRA_POST_ID, post.id)
            args.putStringArrayList(EXTRA_GROUP_ID, post.groupIds.toCollection(ArrayList()))
            args.putSerializable(EXTRA_POST, post)
            return CreateGeneralPostController(args)
        }

        private fun getSharingOptions(args: Bundle): PostPrivacyOptions {
            return when {
                args.containsKey(EXTRA_GROUP) -> {
                    val group = args.getSerializable(EXTRA_GROUP) as GroupModel
                    PostPrivacyOptions(PostPrivacyType.PUBLIC(), emptySet(), setOf(group.id))
                }
                else -> PostPrivacyOptions.DEFAULT
            }
        }

    }

}