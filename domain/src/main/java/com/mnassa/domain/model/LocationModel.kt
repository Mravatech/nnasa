package com.mnassa.domain.model

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/27/2018
 */

interface LocationModel {

    val placeId: String?
    val en: LocationDetailModel?
    val ar: LocationDetailModel?

}

interface LocationDetailModel{
    val city: String?
    val lat: Double?
    val lng: Double?
    val placeId: String?
    val placeName: String?
}
