package com.mnassa.screen.posts.offer.create

import com.mnassa.domain.model.*
import com.mnassa.screen.base.MnassaViewModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 5/3/2018.
 */
interface CreateOfferViewModel : MnassaViewModel, PlaceAutocompleteAdapter.PlaceAutoCompleteListener {
    val closeScreenChannel: BroadcastChannel<Unit>

    suspend fun getTag(tagId: String): TagModel?
    suspend fun getUser(userId: String): ShortAccountModel?
    suspend fun getOfferCategories(): List<OfferCategoryModel>
    suspend fun getOfferSubCategories(category: OfferCategoryModel): List<OfferCategoryModel>
    suspend fun getShareOfferPostPrice(): Long?
    suspend fun getShareOfferPostPerUserPrice(): Long?

    fun applyChanges(post: RawPostModel)
    suspend fun canPromotePost(): Boolean
    suspend fun getPromotePostPrice(): Long
    suspend fun getConnectionsCount(): Long

    suspend fun getUserLocation(): LocationPlaceModel?
}