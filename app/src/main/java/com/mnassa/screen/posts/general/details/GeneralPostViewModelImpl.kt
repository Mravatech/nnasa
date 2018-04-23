package com.mnassa.screen.posts.general.details

import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.screen.posts.need.details.NeedDetailsViewModelImpl

/**
 * Created by Peter on 4/13/2018.
 */
class GeneralPostViewModelImpl(postId: String,
                               postsInteractor: PostsInteractor,
                               tagInteractor: TagInteractor,
                               complaintInteractor: ComplaintInteractor) : NeedDetailsViewModelImpl(
        postId,
        postsInteractor,
        tagInteractor,
        complaintInteractor
), GeneralPostViewModel {
}