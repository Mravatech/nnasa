package com.mnassa.extensions

import android.Manifest
import com.mnassa.activity.CropActivity
import com.mnassa.core.permissions.PermissionsManager

suspend fun PermissionsManager.requestPermissions(imageSource: CropActivity.ImageSource) =
    requestPermissions(
        when (imageSource) {
            CropActivity.ImageSource.GALLERY -> listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            CropActivity.ImageSource.CAMERA -> listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    )
