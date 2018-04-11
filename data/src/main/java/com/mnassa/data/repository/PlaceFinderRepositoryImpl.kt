package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.Places
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.repository.PlaceFinderRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
class PlaceFinderRepositoryImpl(
        private val googleApiClient: GoogleApiClient,
        private val geoPlaceConverter: ConvertersContext) : PlaceFinderRepository {

    override fun getReqieredPlaces(constraint: CharSequence): List<GeoPlaceModel> {
        if (googleApiClient.isConnected) {
            Timber.i("Starting autocomplete query for: $constraint")
            val results = Places.GeoDataApi.getAutocompletePredictions(
                    googleApiClient, constraint.toString(), null, null)
            val autocompletePredictions = results.await(60, TimeUnit.SECONDS)
            val status = autocompletePredictions.status
            if (!status.isSuccess) {
                Timber.e("Error getting autocomplete prediction API call: $status")
                autocompletePredictions.release()
                return emptyList()
            }
            Timber.i("Query completed. Received ${autocompletePredictions.count} predictions.")
            val places = DataBufferUtils.freezeAndClose(autocompletePredictions)
            return geoPlaceConverter.convertCollection(places, GeoPlaceModel::class.java)
        }
        Timber.e("Google API client is not connected for autocomplete query.")
        return emptyList()
    }

}