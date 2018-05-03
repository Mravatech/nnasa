package com.mnassa.screen.posts.offer.create

import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 5/3/2018.
 */
class CreateOfferViewModelImpl(private val offerId: String?, private val postsInteractor: PostsInteractor) : MnassaViewModelImpl(), CreateOfferViewModel {
}