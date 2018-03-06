package com.mnassa.data.network

/**
 * Created by Peter on 2/28/2018.
 */
object NetworkContract {

    object Base {
        const val LANGUAGE_HEADER = "language"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val AUTHORIZATION_HEADER_VALUE_MASK = "Bearer %s"
        const val ACCOUNT_ID_HEADER = "aid"
    }

    object AccountType {
        const val PERSONAL = "personal"
        const val ORGANIZATION = "organization"
    }
}