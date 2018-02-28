package com.mnassa.domain.models.impl.storage

import android.net.Uri
import com.mnassa.domain.models.storage.*
import com.mnassa.domain.models.storage.UploadPhoto

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */

data class UploadPhotoImpl(override val uri: Uri, override val folderType: Int) : UploadPhoto {

    override fun getFolder(): String{
        return when(folderType){
            FOLDER_AVATARS -> AVATARS
            else -> AVATARS
        }
    }
}
