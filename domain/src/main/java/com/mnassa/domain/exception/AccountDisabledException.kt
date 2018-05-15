package com.mnassa.domain.exception

/**
 * Created by Peter on 5/15/2018.
 */
class AccountDisabledException(message: String, cause: Throwable) : NotAuthorizedException(message, cause)