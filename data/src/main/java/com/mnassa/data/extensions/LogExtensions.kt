package com.mnassa.data.extensions

/**
 * Created by Peter on 5/31/2018.
 */
internal inline fun forDebug(crossinline func: () -> Unit) {
    func()
}