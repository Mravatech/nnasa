package com.mnassa.extensions

import com.bluelinelabs.conductor.Controller
import com.mnassa.activity.CropActivity
import com.mnassa.core.BaseController
import com.mnassa.core.permissions.ifAllGranted

suspend fun <T> T.startCropActivityForResult(imageSource: CropActivity.ImageSource, requestId: Int) where T : Controller, T : BaseController<*> {
    permissions
        .requestPermissions(imageSource)
        .ifAllGranted {
            activity?.let {
                val intent = CropActivity.start(imageSource, it)
                startActivityForResult(intent, requestId)
            }
        }
}