package com.mnassa.domain.model.impl

import android.net.Uri
import com.mnassa.domain.model.AVATARS
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.UploadPhoto

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
