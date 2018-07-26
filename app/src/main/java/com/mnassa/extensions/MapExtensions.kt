package com.mnassa.extensions

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Created by Peter on 4/25/2018.
 */
suspend fun GoogleApiClient.getLatLng(placeId: String): LatLng? {
    val result: LatLng? = suspendCancellableCoroutine { continuation ->
        val task = Places.GeoDataApi.getPlaceById(this, placeId)
        task.setResultCallback {
            if (it.status.isSuccess) {
                val place = it.firstOrNull()
                if (place != null) {
                    continuation.resume(place.latLng)
                    return@setResultCallback
                }
            }
            continuation.resume(null)
        }
        continuation.invokeOnCompletion { task.cancel() }
    }
    return result
}

fun GoogleMap.centerOn(latLng: LatLng) {
    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
}