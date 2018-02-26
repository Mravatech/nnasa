package com.mnassa.domain.models

import java.io.Serializable

/**
 * Created by Peter on 2/22/2018.
 */
interface Model : Serializable, HasId {
    override var id: String
}