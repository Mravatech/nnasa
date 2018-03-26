package com.mnassa.domain.model

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */
interface ProfileAccountModel : ShortAccountModel {

    val createdAt: Long?
    val createdAtDate: String?
    val interests: List<String>?
    val offers: List<String>?
    val points : Int?
    val totalIncome : Int?
    val totalOutcome : Int?
}