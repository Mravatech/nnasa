package com.mnassa.domain.model

import android.net.Uri
import com.mnassa.domain.interactor.PostPrivacyOptions
import java.io.Serializable

/**
 * Created by Peter on 5/22/2018.
 */
data class RawPostModel(                            //REQUIRED:
        val id: String? = null,                     //need, general
        val groupId: String? = null,                //need, general
        val text: String,                           //need, general,offer
        val imagesToUpload: List<Uri>,              //need, general,offer
        val uploadedImages: List<String>,           //need, general,offer
        val privacy: PostPrivacyOptions,            //need, general,offer
        val tags: List<TagModel>,                   //need, general,offer
        val price: Long? = null,                    //need          offer
        val timeOfExpiration: Long? = null,         //need
        val placeId: String?,                       //need, general,offer
        val title: String? = null,                  //              offer
        val category: OfferCategoryModel? = null,   //              offer
        val subCategory: OfferCategoryModel? = null,//              offer

        val processedImages: List<String> = emptyList(),
        val processedTags: List<String> = emptyList()
): Serializable

data class RawRecommendPostModel(
        val postId: String?,
        val groupId: String? = null,
        val accountId: String,
        val text: String,
        val privacy: PostPrivacyOptions
): Serializable