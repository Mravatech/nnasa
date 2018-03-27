package com.mnassa.data.network.exception.handler

/**
 * Created by Peter on 3/5/2018.
 */
interface ExceptionHandler {
    fun handle(throwable: Throwable): Throwable
}