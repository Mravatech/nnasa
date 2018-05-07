package com.mnassa.screen.posts.need.create

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.model.PostModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.formatAsMoney
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_need_create.view.*
import kotlinx.coroutines.experimental.Job
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
    override val viewModel: CreateNeedViewModel by instance(arg = postId)
    private var waitForResumeJob: Job? = null
    override var sharingOptions = SharingOptionsController.ShareToOptions.DEFAULT
        set(value) {
            field = value

            waitForResumeJob?.cancel()
            waitForResumeJob = launchCoroutineUI {
                lifecycle.awaitFirst { it == Lifecycle.Event.ON_RESUME }
                view?.tvShareOptions?.text = value.format()
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
                viewModel.createPost(
                        need = etNeed.text.toString(),
                        tags = chipTags.getTags(),
                        images = attachedImagesAdapter.dataStorage.toList(),
                        placeId = placeId,
                        price = etPrice.text.toString().toLongOrNull(),
                        timeOfExpiration = postExpiresIn.millisToExpire,
                        postPrivacyOptions = sharingOptions.asPostPrivacy
                )
            }
            tvShareOptions.setOnClickListener {
                val post = post
                open(SharingOptionsController.newInstance(
                        options = sharingOptions,
                        listener = this@CreateNeedController,
                        accountsToExclude = if (post != null) listOf(post.author.id) else emptyList()))
            }

            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            etNeed.prefix = fromDictionary(R.string.need_create_prefix) + " "
            etNeed.hint = fromDictionary(R.string.need_create_need_placeholder)
            etNeed.addTextChangedListener(SimpleTextWatcher { onNeedTextUpdated() })
            onNeedTextUpdated()

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.chipSearch = viewModel

            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
                actvPlace.setText(placeName)
            }
            tilPlace.hint = fromDictionary(R.string.need_create_city_hint)

            tvExtraDetails.text = fromDictionary(R.string.need_create_extra)
            tilPrice.hint = fromDictionary(R.string.need_create_price_hint)
            launchCoroutineUI {
                postExpiresIn.dayToExpire = viewModel.getDefaultExpirationDays().toString()
            }
            rvImages.adapter = attachedImagesAdapter
        }

        if (args.containsKey(EXTRA_POST_TO_EDIT)) {
            setData(args.getSerializable(EXTRA_POST_TO_EDIT) as PostModel)
            args.remove(EXTRA_POST_TO_EDIT)
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
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
        val permissionsList = when (imageSource) {
            CropActivity.ImageSource.GALLERY -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            CropActivity.ImageSource.CAMERA -> listOf(Manifest.permission.CAMERA)
        }
        val permissionResult = permissions.requestPermissions(permissionsList)
        if (permissionResult.isAllGranted) {
            activity?.let {
                val intent = CropActivity.start(imageSource, it)
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }
        }
    }

    private fun setData(post: PostModel) {
        this.post = post
        with(view ?: return) {
            etNeed.setText(post.text)
            launchCoroutineUI {
                chipTags.setTags(post.tags.mapNotNull { viewModel.getTag(it) })
            }
            attachedImagesAdapter.set(post.images.map { AttachedImage.UploadedImage(it) })

            placeId = post.locationPlace?.placeId
            actvPlace.setText(post.locationPlace?.placeName?.toString())
            etPrice.setText(if (post.price > 0.0) post.price.formatAsMoney().toString() else null)
            sharingOptions.selectedConnections = post.privacyConnections
            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            //no ability to change sharing options while post changing
            tvShareOptions.visibility = View.GONE
        }
    }

    private fun onNeedTextUpdated() {
        val view = view ?: return
        view.toolbar.actionButtonEnabled = view.etNeed.text.length >= MIN_NEED_TEXT_LENGTH
    }

    companion object {
        private const val MIN_NEED_TEXT_LENGTH = 3

        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_POST_TO_EDIT = "EXTRA_POST_TO_EDIT"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"

        fun newInstance() = CreateNeedController(Bundle())
        fun newInstanceEditMode(post: PostModel): CreateNeedController {
            val args = Bundle()
            args.putSerializable(EXTRA_POST_TO_EDIT, post)
            args.putString(EXTRA_POST_ID, post.id)
            return CreateNeedController(args)
        }
    }
}