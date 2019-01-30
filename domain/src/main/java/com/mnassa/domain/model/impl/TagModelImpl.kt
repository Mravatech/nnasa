package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.TranslatedWordModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

@Parcelize
data class TagModelImpl(
        override var status: String?,
        override var name: TranslatedWordModel,
        override var id: String?
) : TagModel

@Parcelize
data class AutoTagModelImpl(
        override var status: String?,
        override var name: TranslatedWordModel,
        override var id: String?
) : TagModel
