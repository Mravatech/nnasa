package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TagModel

/**
 * Created by Peter on 2/22/2018.
 */
data class TagModelImpl(
        override var id: String,
        override val status: String,
        override val nameAr: String,
        override val nameEn: String
) : TagModel