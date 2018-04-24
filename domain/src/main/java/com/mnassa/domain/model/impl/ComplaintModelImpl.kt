package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ComplaintModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
data class ComplaintModelImpl(
        override var id: String,
        override val type: String,
        override val reason: String,
        override val authorText: String?
) : ComplaintModel