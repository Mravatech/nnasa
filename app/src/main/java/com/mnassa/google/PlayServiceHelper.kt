package com.mnassa.google

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
class PlayServiceHelper(private val context: Context) {

    val googleApiClient: GoogleApiClient by lazy {
        val builder = GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
        builder.build()
    }
}