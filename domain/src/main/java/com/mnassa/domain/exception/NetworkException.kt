package com.mnassa.domain.exception

/**
 * Created by Peter on 2/26/2018.
 */
open class NetworkException : RuntimeException {
    val code: Int
    val status: String?
    val errorCode: String?


    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.code = -1
        this.status = null
        this.errorCode = null
    }

    constructor(message: String) : super(message) {
        this.code = -1
        this.status = null
        this.errorCode = null
    }

    constructor(message: String, code: Int, status: String? = null, errorCode: String? = null, cause: Throwable) : super(message, cause) {
        this.code = code
        this.status = status
        this.errorCode = errorCode
    }

    constructor(message: String, code: Int, status: String? = null, errorCode: String? = null) : super(message) {
        this.code = code
        this.status = status
        this.errorCode = errorCode
    }

    override val message: String get() = requireNotNull(super.message)
}