package com.mnassa.core.events

import android.content.Intent

/**
 * Created by Peter on 2/20/2018.
 */
/**
 * Represents an OnActivityResult event
 */
data class OnActivityResultEvent(val requestCode: Int, val resultCode: Int, val data: Intent?)