package com.mnassa.domain.model

import android.net.Uri
import com.mnassa.domain.interactor.PostPrivacyOptions
import java.util.*

/**
 * Created by Peter on 4/26/2018.
 */
data class CreateOrEditEventModel(
        val id: String? = null,
        val title: String,
        val description: String,
        val type: EventType,
        val startDateTime: Date,
        val durationMillis: Long,
        val imagesToUpload: List<Uri>,
        val uploadedImages: MutableSet<String>,
        val privacy: PostPrivacyOptions,
        val ticketsTotal: Int,
        val ticketsPerAccount: Int,
        val price: Long?,
        val locationType: EventLocationType,
        val locationDescription: String?,
        val tagModels: List<TagModel>,
        val tagIds: MutableSet<String> = mutableSetOf()
)