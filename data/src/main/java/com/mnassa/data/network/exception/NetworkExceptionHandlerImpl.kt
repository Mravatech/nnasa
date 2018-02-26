package com.mnassa.data.network.exception

import android.text.TextUtils
import com.google.gson.Gson
import com.mnassa.data.network.bean.retrofit.MnassaErrorBody
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.interactor.DictionaryInteractor
import okhttp3.Headers
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.Charset

/**
 * Created by Peter on 26.02.2018.
 */
class NetworkExceptionHandlerImpl(private val dictionaryInteractor: DictionaryInteractor,
                                  private val gson: Gson) : NetworkExceptionHandler {

    override fun handle(throwable: Throwable): Throwable {
        Timber.d(throwable)

        if (throwable is NetworkException) { //error is already handled
            return throwable
        }

        return if (isNetworkDisabledException(throwable)) {
            NetworkDisableException(dictionaryInteractor.noInternetMessage, throwable)
        } else {
            val errorBody = transformExceptionTo(throwable, MnassaErrorBody::class.java)
            NetworkException(
                    message = errorBody?.error ?: getMessage(throwable) ?: "",
                    code = getCode(throwable),
                    status = errorBody?.status,
                    errorCode = errorBody?.errorCode,
                    cause = throwable
            )
        }

    }

    override fun getCode(throwable: Throwable): Int {
        return (throwable as? HttpException)?.code() ?: NetworkExceptionHandler.UNDEF_ERROR_CODE
    }

    fun getResponseHeaders(throwable: Throwable): Headers? {
        return (throwable as? HttpException)?.response()?.headers()
    }

    override fun getMessage(throwable: Throwable): String? {
        if (throwable is HttpException) {
            val restError = transformExceptionTo(throwable, MnassaErrorBody::class.java)
            return restError?.error ?: dictionaryInteractor.somethingWentWrongMessage
        } else {
            //network error
            if ("Canceled".equals(throwable.cause?.message, ignoreCase = true)) {
                return ""
            } else if (isNetworkDisabledException(throwable)) {
                //no network exception
                return dictionaryInteractor.noInternetMessage
            }
        }

        return dictionaryInteractor.somethingWentWrongMessage
    }

    private fun isNetworkDisabledException(throwable: Throwable): Boolean {
        return throwable is SocketTimeoutException || throwable is UnknownHostException
    }

    /**
     * Error-safe method which transform input throwable into instance of the some class-mapper
     *
     * @param src   input retrofit throwable
     * @param clazz output class-mapper
     * @param <T>   - type ot output class
     * @return result
    </T> */
    protected fun <T> transformExceptionTo(src: Throwable, clazz: Class<T>): T? {
        if (src is HttpException) {

            var responseBody: ResponseBody? = null
            var source: BufferedSource? = null
            var buffer: Buffer? = null

            try {
                responseBody = src.response().errorBody()
                source = responseBody!!.source()

                if (source != null) {
                    source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                    buffer = source.buffer()

                    var charset: Charset? = Charset.defaultCharset()
                    val contentType = responseBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(charset)
                    }
                    val errorString = buffer.clone().readString(charset)
                    if (!TextUtils.isEmpty(errorString)) {
                        return gson.fromJson(errorString, clazz)
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
            } finally {
                responseBody?.close()
                source?.close()
                buffer?.close()
            }
        }
        return null
    }

    protected fun getStatusCode(src: Throwable): Int {
        return (src as? HttpException)?.code() ?: NetworkExceptionHandler.UNDEF_STATUS_CODE
    }
}