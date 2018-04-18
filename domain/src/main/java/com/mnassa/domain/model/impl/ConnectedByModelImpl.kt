package com.mnassa.domain.model.impl

import com.mnassa.domain.model.ConnectedByModel

/**
 * Created by Peter on 4/13/2018.
 */
data class ConnectedByModelImpl(
        override var id: String?,
        override val type: String,
        override val value: String
) : ConnectedByModel