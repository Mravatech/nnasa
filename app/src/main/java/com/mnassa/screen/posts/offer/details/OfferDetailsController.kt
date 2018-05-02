package com.mnassa.screen.posts.offer.details

import android.os.Bundle
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.posts.need.details.NeedDetailsController
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/2/2018.
 */
class OfferDetailsController(args: Bundle) : NeedDetailsController(args) {

    override val viewModel: OfferDetailsViewModel by instance(arg = postId)
    private val popupMenuHelper: PopupMenuHelper by instance()


}