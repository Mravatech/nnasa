package com.mnassa.data.extensions

import com.mnassa.data.BuildConfig

/**
 * Created by Peter on 5/31/2018.
 */
internal inline fun forDebug(crossinline func: () -> Unit) {
    func()
    //if (BuildConfig.DEBUG) func()
}