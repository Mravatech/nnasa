package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.model.ComplaintModel
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.repository.ComplaintRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */

class ComplaintInteractorImpl(
        private val complaintRepository: ComplaintRepository
) : ComplaintInteractor {
    override suspend fun getReports(): List<TranslatedWordModel> = complaintRepository.getReports()

    override suspend fun sendComplaint(complaintModel: ComplaintModel) {
        complaintRepository.sendComplaint(complaintModel)
    }


}