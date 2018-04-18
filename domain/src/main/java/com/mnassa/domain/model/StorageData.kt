package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */
interface StorageData : Serializable {
    val folderType: Int
    fun getFolder(): String
}

// photo quality
// =====================================
const val NORMAL_PHOTO_SIZE = 0
const val MEDIUM_PHOTO_SIZE = 1
const val SMALL_PHOTO_SIZE = 2

const val MEDIUM = "medium_"
const val SMALL = "small_"
const val NORMAL = ""

// folders
// =====================================
const val FOLDER_AVATARS = 0
const val FOLDER_PERSONAL = 1
const val FOLDER_POSTS = 2

const val AVATARS = "avatars/"
const val PERSONAL = "personal/"
const val POSTS = "posts/"
