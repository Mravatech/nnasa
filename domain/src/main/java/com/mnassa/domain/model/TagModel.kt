package com.mnassa.domain.model

import android.os.Parcelable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
interface TagModel : Parcelable {
    var status: String?
    var name: String
    var id: String?
}