package com.mnassa.screen.posts.offer.create

import com.mnassa.domain.interactor.PostPrivacyOptions
import com.mnassa.domain.model.OfferCategoryModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.posts.need.create.AttachedImage
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.widget.ChipsAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/3/2018.
 */
interface CreateOfferViewModel : MnassaViewModel, ChipsAdapter.ChipSearch, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?
    suspend fun getOfferCategories(): List<OfferCategoryModel>
    suspend fun getOfferSubCategories(category: OfferCategoryModel): List<OfferCategoryModel>
    suspend fun getShareOfferPostPrice(): Long?
    suspend fun getShareOfferPostPerUserPrice(): Long?

    fun createPost(
            title: String,
            offer: String,
            category: OfferCategoryModel?,
            subCategory: OfferCategoryModel?,
            tags: List<TagModel>,
            images: List<AttachedImage>,
            placeId: String?,
            price: Long?,
            postPrivacyOptions: PostPrivacyOptions
    )
    suspend fun canPromotePost(): Boolean
    suspend fun getPromotePostPrice(): Long
}