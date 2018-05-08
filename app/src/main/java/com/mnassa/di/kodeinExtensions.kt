package com.mnassa.di

import android.content.Context
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/5/2018.
 */
inline fun <reified T : Any> Context.getInstance(tag: Any? = null): T {
    val kodein by closestKodein(this)
    val result by kodein.instance<T>(tag = tag)
    return result
}