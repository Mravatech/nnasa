package com.mnassa.data.network.exception

import com.mnassa.domain.exception.NetworkException

/**
 * Created by Peter on 3/27/2018.
 */
class NoRightsToComment : NetworkException {
    val canReadComments: Boolean = false
    val canWriteComments: Boolean = false

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(message: String, code: Int, status: String?, errorCode: String?, cause: Throwable) : super(message, code, status, errorCode, cause)
    constructor(message: String, code: Int, status: String?, errorCode: String?) : super(message, code, status, errorCode)
    constructor(copy: NetworkException): this(copy.message, copy.code, copy.status, copy.errorCode, copy)
}