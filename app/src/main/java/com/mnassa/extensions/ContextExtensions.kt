package com.mnassa.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Created by Peter on 3/14/2018.
 */
fun Context.openApplicationSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}