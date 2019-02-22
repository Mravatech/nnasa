package com.mnassa.screen.posts.offer.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.*
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.formatAsMoney
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
import kotlinx.android.synthetic.main.controller_offer_create.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferController(args: Bundle) : MnassaControllerImpl<CreateOfferViewModel>(args),
        SharingOptionsController.OnSharingOptionsResult {
    override val layoutId: Int = R.layout.controller_offer_create
    private val offerId: String? by lazy { args.getString(EXTRA_OFFER_ID) }
    private val groupIds by lazy { args.getStringArrayList(EXTRA_GROUP_ID) ?: emptyList<String>() }
    override val viewModel: CreateOfferViewModel by instance(arg = offerId)
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
                view.toolbar.actionButtonClickable = false
                launchCoroutineUI {
                    viewModel.applyChanges(makePostModel())
                }.invokeOnCompletion { onOfferChanged() }
            }
            tvShareOptions.setOnClickListener(::openShareOptionsScreen)

            applyShareOptionsChanges()
            etOffer.prefix = fromDictionary(R.string.offer_prefix) + " "
            etOffer.hint = fromDictionary(R.string.offer_description_placeholder)
            etOffer.inputType = etOffer.inputType and EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES.inv()
            etOffer.addTextChangedListener(SimpleTextWatcher { onOfferChanged() })
            etTitle.hint = fromDictionary(R.string.offer_title_placeholder)
            etTitle.addTextChangedListener(SimpleTextWatcher { onOfferChanged() })
            onOfferChanged()

            chipTags.tvChipHeader.text = fromDictionary(R.string.need_create_tags_hint)
            chipTags.autodetectTagsFrom(etOffer)

            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            actvPlace.setAdapter(placeAutocompleteAdapter)
            actvPlace.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                placeId = item.placeId
                val placeName = "${item.primaryText} ${item.secondaryText}"
                actvPlace.setText(placeName)
            }
            tilPlace.hint = fromDictionary(R.string.offer_place_placeholder)

            tilPrice.hint = fromDictionary(R.string.offer_price_placeholder)

            rvImages.adapter = attachedImagesAdapter

            showProgress()
            launchCoroutineUI {
                initCategorySpinner()
                if (args.containsKey(EXTRA_OFFER)) {
                    setData(args.getSerializable(EXTRA_OFFER) as OfferPostModel)
                    args.remove(EXTRA_OFFER)
                }
                hideProgress()
            }

            if (offerId != null) {
                toolbar.title = fromDictionary(R.string.offer_edit_title)
            }
        }

        launchCoroutineUI {
            viewModel.closeScreenChannel.consumeEach { close() }
        }

        if (offerId == null && placeId == null) {
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
                    listener = this@CreateOfferController,
                    accountsToExclude = if (post != null) listOf(post.author.id) else emptyList(),
                    restrictShareReduction = offerId != null,
                    canBePromoted = canBePromoted,
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

    private suspend fun initCategorySpinner() {
        with(getViewSuspend()) {
            sCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onOfferChanged()

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    (sCategory.selectedItem as CategoryWrapper?)?.category?.let { category ->
                        if ((sSubCategory.selectedItem as CategoryWrapper?)?.category?.parentId != category.id) {
                            launchCoroutineUI { initSubCategorySpinner(category) }
                        }
                    }
                    onOfferChanged()
                }
            }
            sCategory.adapter = ArrayAdapter(
                    context,
                    R.layout.support_simple_spinner_dropdown_item,
                    android.R.id.text1,
                    viewModel.getOfferCategories().map { CategoryWrapper(it) }
            )
            (sCategory.selectedItem as CategoryWrapper?)?.apply { initSubCategorySpinner(category) }
            onOfferChanged()
        }
    }

    private suspend fun initSubCategorySpinner(category: OfferCategoryModel) {
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

    private suspend fun setData(offer: OfferPostModel) {
        this.post = offer

        with(getViewSuspend()) {
            etTitle.setText(offer.title)
            chipTags.cancelAutodetectTagsFrom(etOffer)
            etOffer.setText(offer.text)
            chipTags.autodetectTagsFrom(etOffer)

            val categoryIndex = findCategoryIndex(offer.category, sCategory)
            if (categoryIndex >= 0) {
                sCategory.setSelection(categoryIndex)
                initSubCategorySpinner((sCategory.selectedItem as CategoryWrapper).category)
            }

            val subCategoryIndex = findCategoryIndex(offer.subCategory, sSubCategory)
            if (subCategoryIndex >= 0) {
                sSubCategory.setSelection(subCategoryIndex)
            }

            chipTags.setTags(offer.tags.mapNotNull { viewModel.getTag(it) })
            attachedImagesAdapter.set(offer.attachments.map { AttachedImage.UploadedImage(it) })

            placeId = offer.locationPlace?.placeId
            actvPlace.setText(offer.locationPlace?.placeName?.toString())

            etPrice.setText(if (offer.price > 0.0) offer.price.formatAsMoney().toString() else null)
            sharingOptions = PostPrivacyOptions(
                offer.privacyType,
                offer.privacyConnections,
                offer.groupIds)
            applyShareOptionsChanges()
        }
    }

    private fun findCategoryIndex(categoryString: String?, categorySpinner: AppCompatSpinner): Int {
        for (categoryPosition in 0 until categorySpinner.adapter.count) {
            val category = (categorySpinner.adapter.getItem(categoryPosition) as CategoryWrapper).category
            if (category.name.engTranslate == categoryString ||
                    category.name.arabicTranslate == categoryString ||
                    category.id == categoryString) {
                return categoryPosition
            }
        }
        return -1
    }

    private suspend fun selectImage(imageSource: CropActivity.ImageSource) {
        startCropActivityForResult(imageSource, REQUEST_CODE_CROP)
    }

    private fun onOfferChanged() {
        view?.toolbar?.actionButtonClickable = canCreatePost()
    }

    private fun canCreatePost(): Boolean {
        return with(view ?: return false) {
            etOffer.text.lengthOrZero >= MIN_OFFER_DESCRIPTION_LENGTH &&
                    etTitle.text.lengthOrZero >= MIN_OFFER_TITLE_LENGTH
        }
    }

    @SuppressLint("SetTextI18n")
    private fun applyShareOptionsChanges() {
        launchCoroutineUI {
            val perPost = viewModel.getShareOfferPostPrice()
            val perPerson = viewModel.getShareOfferPostPerUserPrice() ?: 0L


            if (perPost != null) {
                getViewSuspend().tvShareOptions?.text = "${sharingOptions.format()} ($perPost)"
            } else {
                getViewSuspend().tvShareOptions?.text = when {
                    sharingOptions.privacyType is PostPrivacyType.WORLD -> {
                        val promotePrice = viewModel.getPromotePostPrice()
                        "${sharingOptions.format()} ($promotePrice)"
                    }
                    sharingOptions.privacyType is PostPrivacyType.PUBLIC -> {
                        if (sharingOptions.privacyCommunitiesIds.isEmpty()) {
                            val connectionsCount = viewModel.getConnectionsCount()
                            "${sharingOptions.format()} (${perPerson * connectionsCount})"
                        } else {
                            sharingOptions.format()
                        }
                    }
                    sharingOptions.privacyType is PostPrivacyType.PRIVATE -> {
                        "${sharingOptions.format()} (${perPerson * sharingOptions.privacyConnections.size})"
                    }
                    else -> "(${perPerson * sharingOptions.privacyConnections.size})"
                }
            }
        }
    }

    private fun makePostModel(): RawPostModel {
        return with(requireNotNull(view)) {
            val images = attachedImagesAdapter.dataStorage.toList()
            RawPostModel(
                    id = offerId,
                    groupIds = groupIds,
                    title = etTitle.text.toString(),
                    text = etOffer.text.toString(),
                    category = (sCategory.selectedItem as? CategoryWrapper)?.category,
                    subCategory = (sSubCategory.selectedItem as? CategoryWrapper)?.category,
                    tags = chipTags.getTags(),
                    placeId = placeId,
                    price = etPrice.text.toString().toLongOrNull(),
                    privacy = sharingOptions,
                    imagesToUpload = images.filterIsInstance<AttachedImage.LocalImage>().map { it.imageUri },
                    uploadedImages = images.filterIsInstance<AttachedImage.UploadedImage>().map { it.imageUrl }
            )
        }
    }

    private class CategoryWrapper(val category: OfferCategoryModel) {
        override fun toString(): String = category.name.toString()
    }

    companion object {
        private const val EXTRA_OFFER = "EXTRA_OFFER"
        private const val EXTRA_OFFER_ID = "EXTRA_OFFER_ID"
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val REQUEST_CODE_CROP = 101
        private const val MIN_OFFER_TITLE_LENGTH = 3
        private const val MIN_OFFER_DESCRIPTION_LENGTH = 3

        fun newInstance(group: GroupModel? = null): CreateOfferController {
            val args = Bundle()
            group?.let {
                args.putStringArrayList(EXTRA_GROUP_ID, arrayListOf(it.id))
                args.putSerializable(EXTRA_GROUP, it)
            }
            return CreateOfferController(args)
        }

        fun newInstance(offer: OfferPostModel): CreateOfferController {
            val args = Bundle()
            args.putSerializable(EXTRA_OFFER, offer)
            args.putString(EXTRA_OFFER_ID, offer.id)
            args.putStringArrayList(EXTRA_GROUP_ID, offer.groupIds.toCollection(ArrayList()))
            return CreateOfferController(args)
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