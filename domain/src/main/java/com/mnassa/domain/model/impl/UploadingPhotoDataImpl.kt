package com.mnassa.domain.model.impl

import android.net.Uri
import com.mnassa.domain.model.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */

data class UploadingPhotoDataImpl(override val uri: Uri, override val folderType: Int) : UploadingPhotoData {

    override fun getFolder(): String{
        return when(folderType){
            FOLDER_AVATARS -> AVATARS
            FOLDER_PERSONAL -> PERSONAL
            FOLDER_POSTS -> POSTS
            else -> ""
        }
    }
}
