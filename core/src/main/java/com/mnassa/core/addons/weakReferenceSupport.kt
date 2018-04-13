package com.mnassa.core.addons

/**
 * Created by Peter on 4/5/2018.
 */
import java.lang.ref.WeakReference
import java.util.concurrent.CancellationException
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

class Ref<out T : Any?> internal constructor(obj: T) {
    private val weakRef = WeakReference(obj)

    suspend operator fun invoke(): T {
        return suspendCoroutineOrReturn {
            weakRef.get() ?: throw CancellationException()
        }
    }
}

fun <T : Any?> T.asReference() = Ref(this)