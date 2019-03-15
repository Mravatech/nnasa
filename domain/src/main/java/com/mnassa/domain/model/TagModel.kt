package com.mnassa.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
interface TagModel : Parcelable, Serializable {
    var status: TagStatus?
    var name: TranslatedWordModel
    var id: String?
}

sealed class TagStatus(val value: String) : Parcelable {
    @Parcelize
    object Public : TagStatus(STATUS_PUBLIC)

    @Parcelize
    object Private : TagStatus(STATUS_PRIVATE)

    @Parcelize
    object New : TagStatus(STATUS_NEW)

    companion object {
        private const val STATUS_PUBLIC = "public"
        private const val STATUS_PRIVATE = "private"
        private const val STATUS_NEW = "new"

        fun of(status: String?): TagStatus? =
            when (status) {
                STATUS_PUBLIC -> Public
                STATUS_PRIVATE -> Private
                STATUS_NEW -> New
                else -> null
            }
    }
}