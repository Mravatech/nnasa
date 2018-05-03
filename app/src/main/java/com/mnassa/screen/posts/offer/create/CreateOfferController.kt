package com.mnassa.screen.posts.offer.create

import android.Manifest
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.OfferPostModel
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import kotlinx.android.synthetic.main.controller_offer_create.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferController(args: Bundle) : MnassaControllerImpl<CreateOfferViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_offer_create
    private val offer by lazy { args[EXTRA_OFFER] as OfferPostModel? }
    override val viewModel: CreateOfferViewModel by instance(arg = offer?.id)
    override var sharingOptions = SharingOptionsController.ShareToOptions.DEFAULT
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

    private fun onOfferTextUpdated() {
        with(view ?: return) {
            toolbar.actionButtonEnabled =
                    etOffer.text.length >= MIN_OFFER_DESCRIPTION_LENGTH &&
                    etTitle.text.length >= MIN_OFFER_TITLE_LENGTH
        }

    }

    companion object {
        private const val EXTRA_OFFER = "EXTRA_OFFER"
        private const val REQUEST_CODE_CROP = 101
        private const val MIN_OFFER_TITLE_LENGTH = 3
        private const val MIN_OFFER_DESCRIPTION_LENGTH = 3

        fun newInstance() = CreateOfferController(Bundle())

        fun newInstance(offer: OfferPostModel): CreateOfferController {
            val args = Bundle()
            args.putSerializable(EXTRA_OFFER, offer)
            return CreateOfferController(args)
        }
    }
}