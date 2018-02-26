package com.mnassa.domain.exception

/**
 * Created by Peter on 2/26/2018.
 */
class NetworkException(val status: String, val errorCode: String, val error: String) : RuntimeException(error) {
}