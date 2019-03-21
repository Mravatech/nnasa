package com.mnassa.di

import com.mnassa.core.errorHandler
import com.mnassa.exceptions.handleException

fun registerErrorHandler() {
    errorHandler = ::handleException
}
