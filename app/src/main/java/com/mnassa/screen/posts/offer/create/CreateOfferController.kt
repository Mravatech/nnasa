package com.mnassa.screen.posts.offer.create

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.OfferCategoryModel
import com.mnassa.domain.model.OfferPostModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.posts.need.create.AttachedImagesRVAdapter
import com.mnassa.screen.posts.need.sharing.SharingOptionsController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_offer_create.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferController(args: Bundle) : MnassaControllerImpl<CreateOfferViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_offer_create
    private val offerId: String? by lazy { args.getString(EXTRA_OFFER_ID) }
    override val viewModel: CreateOfferViewModel by instance(arg = offerId)
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
    private var post: OfferPostModel? = null

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
//                viewModel.createPost(
//                        need = etNeed.text.toString(),
//                        tags = chipTags.getTags(),
//                        images = attachedImagesAdapter.dataStorage.toList(),
//                        placeId = placeId,
//                        price = etPrice.text.toString().toLongOrNull(),
//                        postPrivacyOptions = sharingOptions.asPostPrivacy
//                )
            }
            tvShareOptions.setOnClickListener {
                val post = post
                open(SharingOptionsController.newInstance(
                        options = sharingOptions,
                        listener = this@CreateOfferController,
                        accountsToExclude = if (post != null) listOf(post.author.id) else emptyList()))
            }

            launchCoroutineUI {
                tvShareOptions.text = sharingOptions.format()
            }
            etOffer.prefix = fromDictionary(R.string.offer_prefix) + " "
            etOffer.hint = fromDictionary(R.string.offer_description_placeholder)
            etOffer.addTextChangedListener(SimpleTextWatcher { onOfferChanged() })
            etTitle.hint = fromDictionary(R.string.offer_title_placeholder)
            etTitle.addTextChangedListener(SimpleTextWatcher { onOfferChanged() })
            onOfferChanged()

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
            tilPlace.hint = fromDictionary(R.string.offer_place_placeholder)

            tilPrice.hint = fromDictionary(R.string.offer_price_placeholder)

            rvImages.adapter = attachedImagesAdapter

            initCategorySpinner()
        }

        if (args.containsKey(EXTRA_OFFER)) {
            setData(args.getSerializable(EXTRA_OFFER) as OfferPostModel)
            args.remove(EXTRA_OFFER)
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
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

    private fun initCategorySpinner() {
        launchCoroutineUI {
            with(getViewSuspend()) {
                sCategory.adapter = ArrayAdapter(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        android.R.id.text1,
                        viewModel.getOfferCategories().map { CategoryWrapper(it) }
                )
                sCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = onOfferChanged()

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        (sCategory.selectedItem as CategoryWrapper?)?.apply { initSubCategorySpinner(category) }
                        onOfferChanged()
                    }
                }
            }
        }
    }

    private fun initSubCategorySpinner(category: OfferCategoryModel) {
        launchCoroutineUI {
            with(getViewSuspend()) {
                sSubCategory.adapter = ArrayAdapter(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        android.R.id.text1,
                        viewModel.getOfferSubCategories(category).map { CategoryWrapper(it) }
                )
                sSubCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = onOfferChanged()

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        (sSubCategory.selectedItem as CategoryWrapper?)?.run {
                            //do something
                        }
                    }
                }
            }
        }
    }

    private fun setData(offer: OfferPostModel) {
        this.post = offer



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

    private fun onOfferChanged() {
        with(view ?: return) {
            toolbar.actionButtonEnabled =
                    etOffer.text.length >= MIN_OFFER_DESCRIPTION_LENGTH &&
                    etTitle.text.length >= MIN_OFFER_TITLE_LENGTH
        }

    }

    private class CategoryWrapper(val category: OfferCategoryModel) {
        override fun toString(): String = category.name.toString()
    }

    companion object {
        private const val EXTRA_OFFER = "EXTRA_OFFER"
        private const val EXTRA_OFFER_ID = "EXTRA_OFFER_ID"
        private const val REQUEST_CODE_CROP = 101
        private const val MIN_OFFER_TITLE_LENGTH = 3
        private const val MIN_OFFER_DESCRIPTION_LENGTH = 3

        fun newInstance() = CreateOfferController(Bundle())

        fun newInstance(offer: OfferPostModel): CreateOfferController {
            val args = Bundle()
            args.putSerializable(EXTRA_OFFER, offer)
            args.putString(EXTRA_OFFER_ID, offer.id)
            return CreateOfferController(args)
        }
    }
}