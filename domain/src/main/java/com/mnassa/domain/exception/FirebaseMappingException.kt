package com.mnassa.domain.exception

/**
 * Created by Peter on 4/25/2018.
 */
class FirebaseMappingException(val path: String, throwable: Throwable) : IllegalStateException("Firebase mapping exception path: [$path]", throwable) {
}