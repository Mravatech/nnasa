package com.mnassa.screen.comments

import com.mnassa.screen.posts.need.recommend.RecommendController

/**
 * Created by Peter on 4/17/2018.
 */
interface CommentsWrapperListener : RecommendController.OnRecommendPostResult {
    fun openKeyboardOnComment()
}