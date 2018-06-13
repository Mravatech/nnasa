package com.mnassa.domain.model

import android.os.Parcelable
import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
interface TagModel : Parcelable, Serializable {
    var status: String?
    var name: TranslatedWordModel
    var id: String?
    var localId: Long
}