package com.mnassa.data.network.exception

import com.google.gson.Gson
import com.mnassa.data.network.bean.retrofit.MnassaErrorBody
import com.mnassa.domain.exception.NetworkDisableException
import com.mnassa.domain.exception.NetworkException
import com.mnassa.domain.interactor.DictionaryInteractor
import okhttp3.Headers
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

        return when {
            throwable is NetworkException -> throwable
            isNetworkDisabledException(throwable) -> NetworkDisableException(dictionaryInteractor.noInternetMessage, throwable)
            else -> {
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
    }

    override fun getCode(throwable: Throwable): Int {
        return (throwable as? HttpException)?.code() ?: NetworkExceptionHandler.UNDEF_ERROR_CODE
    }

    fun getResponseHeaders(throwable: Throwable): Headers? {
        return (throwable as? HttpException)?.response()?.headers()
    }

    override fun getMessage(throwable: Throwable): String? = when {
        throwable is HttpException ->
            transformExceptionTo(throwable, MnassaErrorBody::class.java)
                    ?.error ?: dictionaryInteractor.somethingWentWrongMessage
        "Canceled".equals(throwable.cause?.message, ignoreCase = true) -> null
        isNetworkDisabledException(throwable) -> dictionaryInteractor.noInternetMessage
        else -> dictionaryInteractor.somethingWentWrongMessage
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
        if (src !is HttpException) return null

        try {
            src.response().errorBody()?.use { responseBody ->
                responseBody.source()?.use { source ->
                    source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
                    source.buffer().use { buffer ->
                        var charset: Charset? = Charset.defaultCharset()
                        responseBody.contentType()?.apply {
                            charset = charset(charset)
                        }
                        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                        val errorString = buffer.clone().readString(charset)

                        if (errorString.isNotBlank()) {
                            return gson.fromJson(errorString, clazz)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Timber.d(e)
        }
        return null
    }

    protected fun getStatusCode(src: Throwable): Int {
        return (src as? HttpException)?.code() ?: NetworkExceptionHandler.UNDEF_STATUS_CODE
    }
}