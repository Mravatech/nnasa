package com.mnassa.extensions

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.activity.CropActivity
import com.mnassa.core.BaseController
import com.mnassa.core.addons.launchUI
import com.mnassa.core.permissions.ifAllGranted
import com.mnassa.screen.base.MnassaControllerImpl

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

suspend inline fun <T: MnassaControllerImpl<*>> T.onClick(crossinline onClick: suspend (View) -> Unit) : (View) -> Unit {
    return { view ->
       launchUI {
           onClick(view)
       }
    }
}
