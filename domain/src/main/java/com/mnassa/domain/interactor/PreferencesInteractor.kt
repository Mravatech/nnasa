package com.mnassa.domain.interactor

/**
 * Created by Peter on 7/23/2018.
 */
interface PreferencesInteractor {
    fun saveString(key: String, value: String?)
    fun getString(key: String): String?

}