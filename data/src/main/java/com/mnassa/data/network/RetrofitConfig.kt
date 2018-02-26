package com.mnassa.data.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Peter on 2/26/2018.
 */
class RetrofitConfig(
        private val appInfoProvider: AppInfoProvider,
        private val languageProvider: LanguageProvider,
        private val userProfileInteractor: UserProfileInteractor,
        private val gson: Gson) {

    fun makeRetrofit(): Retrofit {
        val baseUrl = appInfoProvider.endpoint

        val okHttpBuilder = OkHttpClient.Builder()

        if (appInfoProvider.isDebug) {
            okHttpBuilder.addNetworkInterceptor(StethoInterceptor())
            okHttpBuilder.addInterceptor(HttpLoggingInterceptor())
        }

        okHttpBuilder.addInterceptor { chain ->
            val request = chain.request()
            val newRequest: Request.Builder = request.newBuilder()

            newRequest.addHeader("language", languageProvider.language)

            runBlocking {
                userProfileInteractor.getToken()?.let { newRequest.addHeader("Authorization", "Bearer $it") }
                userProfileInteractor.getAccountId()?.let { newRequest.addHeader("aid", it) }
            }

            chain.proceed(newRequest.build())
        }

        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))

        return retrofitBuilder.client(okHttpBuilder.build()).build()
    }

}