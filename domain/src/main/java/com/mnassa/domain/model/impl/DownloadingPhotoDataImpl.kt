package com.mnassa.domain.model.impl

import android.support.annotation.IntRange
import com.mnassa.domain.model.DownloadingPhotoData
import com.mnassa.domain.model.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */

data class DownloadingPhotoDataImpl(
        @IntRange(from = 0, to = 2) override val size: Int,
        override val folderType: Int) : DownloadingPhotoData {

    override fun getFolder(): String {
        return when (folderType) {
            MEDIUM_PHOTO_SIZE -> MEDIUM
            SMALL_PHOTO_SIZE -> SMALL
            else -> NORMAL
        }
    }

    fun getPhotoSize(): String {
        return when (size) {
            MEDIUM_PHOTO_SIZE -> MEDIUM
            SMALL_PHOTO_SIZE -> SMALL
            else -> NORMAL
        }
    }
}