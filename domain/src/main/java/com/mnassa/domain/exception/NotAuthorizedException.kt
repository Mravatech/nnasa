package com.mnassa.domain.exception

/**
 * Created by Peter on 4/6/2018.
 */
class NotAuthorizedException(message: String, cause: Throwable): NetworkException(message, cause)