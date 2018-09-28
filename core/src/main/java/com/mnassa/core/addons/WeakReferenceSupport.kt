package com.mnassa.core.addons

/**
 * Created by Peter on 4/5/2018.
 */
import java.lang.ref.WeakReference
import java.util.concurrent.CancellationException
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

class Ref<T : Any?> internal constructor(obj: T) : WeakReference<T>(obj) {

    suspend operator fun invoke(): T {
        return suspendCoroutineOrReturn {
            get() ?: throw CancellationException()
        }
    }

    operator fun invoke(body: T.() -> Unit) = get()?.apply(body)
}

fun <T : Any?> T.asReference() = Ref(this)
