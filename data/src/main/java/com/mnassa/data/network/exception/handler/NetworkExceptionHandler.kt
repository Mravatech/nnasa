package com.mnassa.data.network.exception.handler

/**
 * Created by Peter on 26.02.2018.
 */
interface NetworkExceptionHandler {
    fun handle(throwable: Throwable): Throwable

    fun getCode(throwable: Throwable): Int

    fun getMessage(throwable: Throwable): String?

    companion object {
        const val UNDEF_ERROR_CODE = Integer.MIN_VALUE
        const val UNDEF_STATUS_CODE = Integer.MIN_VALUE
    }
}