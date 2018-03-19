package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TagModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

data class TagModelImpl(
        override var status: String?,
        override var name: String,
        override var id: String?
) : TagModel
