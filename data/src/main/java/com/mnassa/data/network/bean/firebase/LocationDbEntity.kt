package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/15/2018.
 */
internal data class LocationDbEntity(
        @SerializedName("ar")
        val ar: LocationTranslateDbEntity?,
        @SerializedName("en")
        val en: LocationTranslateDbEntity?,
        @SerializedName("placeId")
        var placeId: String
)

internal data class LocationTranslateDbEntity(
        @SerializedName("city")
        val city: String?,
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lng")
        val lng: Double,
        @SerializedName("placeId")
        val placeId: String,
        @SerializedName("placeName")
        val placeName: String?
)