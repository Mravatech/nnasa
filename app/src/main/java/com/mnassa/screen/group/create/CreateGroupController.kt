package com.mnassa.screen.group.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.RawGroupModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.image
import com.mnassa.extensions.isGone
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_group_create.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 5/22/2018.
 */

class CreateGroupController(args: Bundle) : MnassaControllerImpl<CreateGroupViewModel>(args) {
    override val layoutId: Int = R.layout.controller_group_create
    private val groupId: String? by lazy { args.getString(EXTRA_GROUP_ID, null) }
    override val viewModel: CreateGroupViewModel by instance(arg = groupId)
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialogHelper: DialogHelper by instance()
    private var placeId: String? = null
    private var avatar: AttachedImage? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()

        with(view) {
            tilGroupTitle.hint = fromDictionary(R.string.group_title_placeholder)
            tilGroupDescription.hint = fromDictionary(R.string.group_description_placeholder)
            tilGroupWebsite.hint = fromDictionary(R.string.group_website_placeholder)
            tilPlace.hint = fromDictionary(R.string.group_place_placeholder)

            etGroupTitle.addTextChangedListener(SimpleTextWatcher { onInputChanged() })

            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
                actvPlace.setText(placeName)
            }

            val listener = View.OnClickListener {
                dialogHelper.showSelectImageSourceDialog(view.context) { imageSource -> launchCoroutineUI { selectImage(imageSource) } }
            }
            vAddImage.setOnClickListener(listener)
            btnReplace.setOnClickListener(listener)
            btnDelete.setOnClickListener { bindAvatar(null, view) }

            btnReplace.text = fromDictionary(R.string.need_create_replace_photo)
            btnDelete.text = fromDictionary(R.string.need_create_delete_photo)

            toolbar.withActionButton(fromDictionary(R.string.group_save_changes)) {
                viewModel.applyChanges(makeGroupModel())
            }

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.chipSearch = viewModel
        }

        launchCoroutineUI { viewModel.closeScreenChanel.consumeEach { close() } }

        if (args.containsKey(EXTRA_GROUP_TO_EDIT)) {
            bindGroup(args.getSerializable(EXTRA_GROUP_TO_EDIT) as GroupModel, view)
            args.remove(EXTRA_GROUP_TO_EDIT)
        }

        onInputChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_CROP) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri? = data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                uri?.let {
                    launchCoroutineUI { bindAvatar(AttachedImage.LocalImage(uri), getViewSuspend()) }
                }
            }
            CropActivity.GET_PHOTO_ERROR -> {
                this.avatar = null
                Timber.e("CropActivity.GET_PHOTO_ERROR")
            }
        }
    }

    private fun bindGroup(group: GroupModel, view: View) {
        with(view) {
            toolbar.title = fromDictionary(R.string.group_edit_title)
            etGroupTitle.setText(group.name)
            etGroupDescription.setText(group.description)
            etGroupWebsite.setText(group.website)

            placeId = group.locationPlace?.placeId
            actvPlace.setText(group.locationPlace?.placeName?.toString())

            launchCoroutineUI { chipTags.setTags(group.tags.mapNotNull { viewModel.getTag(it) }) }

            bindAvatar(group.avatar?.let { AttachedImage.UploadedImage(it) }, view)
        }
    }

    private fun bindAvatar(avatar: AttachedImage?, view: View) {
        this.avatar = avatar
        with(view) {
            cvChangeImage.isGone = avatar == null
            cvAddImage.isGone = avatar != null
            when (avatar) {
                is AttachedImage.UploadedImage -> ivImage.image(avatar.imageUrl)
                is AttachedImage.LocalImage -> ivImage.image(avatar.imageUri)
            }
        }
        onInputChanged()
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        val permissionsList = when (imageSource) {
            CropActivity.ImageSource.GALLERY -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            CropActivity.ImageSource.CAMERA -> listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        val permissionResult = permissions.requestPermissions(permissionsList)
        if (permissionResult.isAllGranted) {
            activity?.let {
                val intent = CropActivity.start(imageSource, it, cropSquare = true)
                startActivityForResult(intent, REQUEST_CODE_CROP)
            }
        }
    }

    override fun onViewDestroyed(view: View) {
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        super.onViewDestroyed(view)
    }

    private fun makeGroupModel(): RawGroupModel {
        return with(requireNotNull(view)) {
            RawGroupModel(
                    id = groupId,
                    website = etGroupWebsite.text.toString().takeIf { it.isNotBlank() },
                    title = etGroupTitle.text.toString(),
                    description = etGroupDescription.text.toString().takeIf { it.isNotBlank() },
                    placeId = placeId,
                    avatarToUpload = (avatar as? AttachedImage.LocalImage)?.imageUri,
                    avatarUploaded = (avatar as? AttachedImage.UploadedImage)?.imageUrl,
                    tags = chipTags.getTags()
            )
        }
    }

    private fun onInputChanged() {
        view?.toolbar?.actionButtonClickable = !view?.etGroupTitle?.text.isNullOrBlank() && avatar != null
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_GROUP_TO_EDIT = "EXTRA_GROUP_TO_EDIT"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"

        fun newInstance() = CreateGroupController(Bundle())

        fun newInstance(group: GroupModel): CreateGroupController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, group.id)
            args.putSerializable(EXTRA_GROUP_TO_EDIT, group)
            return CreateGroupController(args)
        }
    }
}