package com.mnassa.domain.models.impl

import com.mnassa.domain.models.TagModel

/**
 * Created by Peter on 2/22/2018.
 */
data class TagModelImpl(
        override var id: String,
        override val status: String,
        override val nameAr: String,
        override val nameEn: String
) : TagModel