package com.mnassa.other

import android.support.annotation.IntRange

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/2/2018
 */
interface TakingPhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}