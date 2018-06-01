package com.mnassa.domain.model

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */

interface ComplaintModel : Model {
    override var id: String
    val type: String
    val reason: String
    val authorText: String?
}
