package com.mnassa.data.network.bean.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */
data class RegisterSendAccountInfoRequestTest(
        @SerializedName("birthdayDate")
        val birthdayDate: String? = null,
        @SerializedName("locationId")
        val locationId: String? = null,
        @SerializedName("lastName")
        val lastName: String? = null,
        @SerializedName("userName")
        val userName: String? = null,
        @SerializedName("showContactEmail")
        val showContactEmail: Boolean? = false,
        @SerializedName("language")
        val language: String? = null,
        @SerializedName("type")
        val type: String? = null,
        @SerializedName("birthday")
        val birthday: Double? = 0.0,
        @SerializedName("contactPhone")
        val contactPhone: String? = null,
        @SerializedName("abilities")
        val abilities: List<Ability>? = null,
        @SerializedName("location")
        val location: Location? = null,
        @SerializedName("id")
        val id: String? = null,
        @SerializedName("avatar")
        val avatar: String? = null,
        @SerializedName("firstName")
        val firstName: String? = null,
//        @SerializedName("offers")
//        val offers: List<String>? = null,
        @SerializedName("interests")
        val interests: List<String>? = null,
        @SerializedName("showContactPhone")
        val showContactPhone: Boolean? = false
)

data class Ability(
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("place")
        val place: String? = null,
        @SerializedName("isMain")
        val isMain: Boolean? = false
)

data class Location(
        @SerializedName("placeId")
        val placeId : String? = null

)
//
//["birthdayDate": "1989-05-15 20:00:00",
//"locationId": "ChIJj0YI_QPj20ARuhrB8tXzHAo",
//"lastName": "Che",
//"userName": "CC",
//"showContactEmail": true,
//"language": "en",
//"type": "personal",
//"birthday": 611265600000.0,
//"contactPhone": "+380951299232",
//"abilities": [
//["name": "Employee",
//"place": "",
//"isMain": true],
//["name": "Business owner",
//"place": "exness "]
//],
//"location": ["placeId": "ChIJj0YI_QPj20ARuhrB8tXzHAo"],
//"id": "-L5o1CwYOYC0WIHNfiFq",
//"avatar": "gs://fir-test-b7667.appspot.com/avatars/7hGkZys66BdXeFk9G3kKv91ZvQ13/-L5o6dFYTck9JomyNJuP.jpg",
//"firstName": "Chas",
//"offers": ["-L5o3gRz9DfDXkdTZ01B", "-L4u2CEhgz50g3e9VUQA", "-L59KYGkLgK_e3cSNQzC", "-L612mSd0W-BKUpkpRNf", "-L46JzLZs04RORHAbyHp"],
//"interests": ["-L59C0y19-aGFdDN8kNc"],
//"showContactPhone": false]