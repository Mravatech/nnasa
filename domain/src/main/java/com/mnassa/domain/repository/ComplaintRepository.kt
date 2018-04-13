package com.mnassa.domain.repository

import com.mnassa.domain.model.ComplaintModel
import com.mnassa.domain.model.TranslatedWordModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */

interface ComplaintRepository {
    suspend fun sendComplaint(complaintModel: ComplaintModel)
    suspend fun getReports(): List<TranslatedWordModel>
}