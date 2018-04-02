package com.mnassa.data.network.exception.handler

import android.content.Context
import com.google.gson.Gson
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.exception.NoRightsToComment
import com.mnassa.domain.exception.NetworkException

/**
 * Created by Peter on 3/27/2018.
 */
class CommentsExceptionHandler(gson: Gson, private val context: Context) : NetworkExceptionHandlerImpl(gson, context) {
    override fun handle(throwable: Throwable): Throwable {
        val handledException = super.handle(throwable)
        return when {
            handledException is NetworkException
                    && handledException.code == NetworkContract.ResponseCode.NO_RIGHTS_TO_COMMENT
                    && handledException.errorCode == NetworkContract.ErrorCode.NO_RIGHTS_TO_COMMENT -> NoRightsToComment(handledException)
            else -> handledException
        }
    }
}