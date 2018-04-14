package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.firebase.database.DatabaseReference
import com.mnassa.data.extensions.awaitList
import com.mnassa.data.network.api.FirebaseComplaintApi
import com.mnassa.data.network.bean.firebase.TranslatedWordDbEntity
import com.mnassa.data.network.bean.retrofit.request.ComplaintRequest
import com.mnassa.data.network.exception.handler.ExceptionHandler
import com.mnassa.data.network.exception.handler.handleException
import com.mnassa.data.repository.DatabaseContract.COMPLAINT_REASON
import com.mnassa.data.repository.DatabaseContract.TABLE_DICTIONARY
import com.mnassa.domain.model.ComplaintModel
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.repository.ComplaintRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */

class ComplaintRepositoryImpl(
        private val db: DatabaseReference,
        private val exceptionHandler: ExceptionHandler,
        private val converter: ConvertersContext,
        private val complaintApi: FirebaseComplaintApi) : ComplaintRepository {


    override suspend fun sendComplaint(complaintModel: ComplaintModel) {
        complaintApi.inappropriate(
                ComplaintRequest(complaintModel.id, complaintModel.type, complaintModel.reason)
        ).handleException(exceptionHandler)
    }

    override suspend fun getReports(): List<TranslatedWordModel> {
        val translatedDbWord = db.child(TABLE_DICTIONARY)
                .child(COMPLAINT_REASON)
                .awaitList<TranslatedWordDbEntity>(exceptionHandler)
        return converter.convertCollection(translatedDbWord, TranslatedWordModel::class.java)
    }


}