package com.mnassa.screen.comments

import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
data class CommentsRewardModel(val canReward: Boolean, val isOwner: Boolean) : Serializable