package com.mnassa.domain.models.storage

import android.net.Uri

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */
interface UploadPhoto : StorageData{
    val uri: Uri
}