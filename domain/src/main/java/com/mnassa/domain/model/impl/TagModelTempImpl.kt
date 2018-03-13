package com.mnassa.domain.model.impl

import com.mnassa.domain.model.TagModelTemp

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

data class TagModelTempImpl(
        override var status: String?,
        override var name: String,
        override var id: String?
) : TagModelTemp
