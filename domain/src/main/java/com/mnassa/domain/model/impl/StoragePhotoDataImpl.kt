package com.mnassa.domain.model.impl

import android.net.Uri
import com.mnassa.domain.model.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */

data class StoragePhotoDataImpl(override val uri: Uri, override val folderType: Int) : StoragePhotoData {

    override fun getFolder(): String{
        return when(folderType){
            FOLDER_AVATARS -> AVATARS
            FOLDER_PERSONAL -> PERSONAL
            FOLDER_POSTS -> POSTS
            FOLDER_EVENTS -> EVENTS
            else -> ""
        }
    }
}
