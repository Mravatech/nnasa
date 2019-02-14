package com.mnassa.domain.model

/**
 * @author Artem Chepurnoy
 */
interface HasIdMaybe : HasId {

    var idOrNull: String?

    override var id: String
        get() = idOrNull ?: EMPTY_KEY
        set(value) {
            idOrNull = value
        }

    companion object {
        const val EMPTY_KEY = ""
    }

}