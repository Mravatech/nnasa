package com.mnassa.screen.posts.need.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.permissions.ifAllGranted
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.PostPrivacyType
import com.mnassa.domain.model.RawPostModel
import com.mnassa.extensions.*
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.posts.need.sharing.format
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_need_create.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedController(args: Bundle) : MnassaControllerImpl<CreateNeedViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_need_create
    private val postId: String? by lazy { args.getString(EXTRA_POST_ID, null) }
    private val groupIds by lazy { args.getStringArrayList(EXTRA_GROUP_ID) ?: emptyList<String>() }
    override val viewModel: CreateNeedViewModel by instance(arg = postId)
    override var sharingOptions = getSharingOptions(args)
        set(value) {
            field = value
            launchCoroutineUI {
                getViewSuspend().tvShareOptions?.text = value.format()
            }
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

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.need_create_action_button)) {
                view.toolbar.actionButtonClickable = false
                launchCoroutineUI {
                    viewModel.applyChanges(makePostModel(view))
                }.invokeOnCompletion { onNeedTextUpdated() }
            }
            tvShareOptions.setOnClickListener(::openShareOptionsScreen)

            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            etNeed.prefix = fromDictionary(R.string.need_create_prefix) + " "
            etNeed.hint = fromDictionary(R.string.need_create_need_placeholder)
            etNeed.inputType = etNeed.inputType and EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES.inv()
            etNeed.addTextChangedListener(SimpleTextWatcher { onNeedTextUpdated() })
            onNeedTextUpdated()

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.autodetectTagsFrom(etNeed)

            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${item.secondaryText}"
                actvPlace.setText(placeName)
            }
            tilPlace.hint = fromDictionary(R.string.need_create_city_hint)

            tvExtraDetails.text = fromDictionary(R.string.need_create_extra)
            tilPrice.hint = fromDictionary(R.string.need_create_price_hint)
            launchCoroutineUI {
                postExpiresIn.dayTo = viewModel.getDefaultExpirationDays().toString()
            }
            rvImages.adapter = attachedImagesAdapter
        }

        if (args.containsKey(EXTRA_POST_TO_EDIT)) {
            setData(args.getSerializable(EXTRA_POST_TO_EDIT) as PostModel, view)
            args.remove(EXTRA_POST_TO_EDIT)
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
            val canPromote = viewModel.canPromotePost()
            val promotePrice = viewModel.getPromotePostPrice()
            open(SharingOptionsController.newInstance(
                    options = sharingOptions,
                    listener = this@CreateNeedController,
                    accountsToExclude = if (post != null) listOf(post.author.id) else emptyList(),
                    restrictShareReduction = postId != null,
                    canBePromoted = canPromote,
                    promotePrice = promotePrice))
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

    private fun makePostModel(view: View): RawPostModel {
        with(view) {
            val images = attachedImagesAdapter.dataStorage.toList()
            return RawPostModel(
                    id = postId,
                    groupIds = groupIds,
                    text = etNeed.text.toString(),
                    imagesToUpload = images.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri },
                    uploadedImages = images.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl },
                    privacy = sharingOptions,
                    tags = chipTags.getTags(),
                    price = etPrice.text.toString().toLongOrNull(),
                    timeOfExpiration = postExpiresIn.millisTo,
                    placeId = placeId
            )
        }
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        startCropActivityForResult(imageSource, REQUEST_CODE_CROP)
    }

    private fun setData(post: PostModel, view: View) {
        this.post = post
        with(view) {
            etNeed.setText(post.text)
            attachedImagesAdapter.set(post.attachments.map { AttachedImage.UploadedImage(it) })

            placeId = post.locationPlace?.placeId
            actvPlace.setText(post.locationPlace?.placeName?.toString())
            etPrice.setText(if (post.price > 0.0) post.price.formatAsMoney().toString() else null)
            sharingOptions = PostPrivacyOptions(
                post.privacyType,
                post.privacyConnections,
                post.groupIds)
            launchCoroutineUI { tvShareOptions.text = sharingOptions.format() }
            launchCoroutineUI { chipTags.setTags(post.tags.mapNotNull { viewModel.getTag(it) }) }
        }
    }

    private fun onNeedTextUpdated() {
        view?.toolbar?.actionButtonClickable = canCreatePost()
    }

    private fun canCreatePost(): Boolean {
        val view = view ?: return false
        return view.etNeed.text.lengthOrZero >= MIN_NEED_TEXT_LENGTH
    }

    companion object {
        private const val MIN_NEED_TEXT_LENGTH = 3

        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"

        fun newInstance(group: GroupModel? = null): CreateNeedController {
            val args = Bundle()
            group?.let {
                args.putStringArrayList(EXTRA_GROUP_ID, arrayListOf(it.id))
                args.putSerializable(EXTRA_GROUP, it)
            }
            return CreateNeedController(args)
        }

        fun newInstanceEditMode(post: PostModel): CreateNeedController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST_TO_EDIT, post)
            args.putString(EXTRA_POST_ID, post.id)
            args.putStringArrayList(EXTRA_GROUP_ID, post.groupIds.toCollection(ArrayList()))
            return CreateNeedController(args)
        }

        private fun getSharingOptions(args: Bundle): PostPrivacyOptions {
            return when {
                args.containsKey(EXTRA_GROUP) -> {
                    val group = args.getSerializable(EXTRA_GROUP) as GroupModel
                    PostPrivacyOptions(PostPrivacyType.PUBLIC(), emptySet(), setOf(group.id))
                }
                args.containsKey(EXTRA_POST_TO_EDIT) -> {
                    val post = args.getSerializable(EXTRA_POST_TO_EDIT) as PostModel
                    PostPrivacyOptions(post.privacyType, post.privacyConnections, emptySet())
                }
                else -> PostPrivacyOptions.DEFAULT
            }
        }
    }
}