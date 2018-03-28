package com.mnassa.domain.model.impl

import com.mnassa.domain.model.LocationDetailModel
import com.mnassa.domain.model.LocationModel
import kotlinx.android.parcel.Parcelize

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/27/2018
 */

@Parcelize
data class LocationModelImpl(
        override val placeId: String?,
        override val en: LocationDetailModel?,
        override val ar: LocationDetailModel?) : LocationModel
@Parcelize
data class LocationDetailModelImpl(
        override val city: String?,
        override val lat: Double?,
        override val lng: Double?,
        override val placeId: String?,
        override val placeName: String?) : LocationDetailModel