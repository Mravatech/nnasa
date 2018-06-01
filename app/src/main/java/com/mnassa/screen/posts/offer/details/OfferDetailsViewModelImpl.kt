package com.mnassa.screen.posts.offer.details

import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.screen.posts.need.details.NeedDetailsViewModel
import com.mnassa.screen.posts.need.details.NeedDetailsViewModelImpl

/**
 * Created by Peter on 5/2/2018.
 */
class OfferDetailsViewModelImpl(
        params: NeedDetailsViewModel.ViewModelParams,
        postsInteractor: PostsInteractor,
        tagInteractor: TagInteractor,
        complaintInteractor: ComplaintInteractor
) : NeedDetailsViewModelImpl(params, postsInteractor, tagInteractor, complaintInteractor), OfferDetailsViewModel {
}