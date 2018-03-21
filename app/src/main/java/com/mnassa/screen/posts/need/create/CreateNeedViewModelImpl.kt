package com.mnassa.screen.posts.need.create

import android.os.Bundle
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.TagModel
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 3/19/2018.
 */
class CreateNeedViewModelImpl(
        private val postsInteractor: PostsInteractor,
        private val tagInteractor: TagInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor
) : MnassaViewModelImpl(), CreateNeedViewModel {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override suspend fun getTag(tagId: String): TagModel? = tagInteractor.get(tagId)

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
    override suspend fun search(search: String): List<TagModel> = tagInteractor.search(search)
}