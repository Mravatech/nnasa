package com.mnassa.domain.model

/**
 * Created by Peter on 5/3/2018.
 */
data class OfferCategoryModel(
        override var id: String,
        val name: TranslatedWordModel,
        val parentId: String?
) : Model {
    val isCategory: Boolean get() = parentId == null
    val isSubCategory: Boolean get() = !isCategory
}