package com.mnassa.data.network

import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.domain.other.LanguageProvider
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit



/**
 * Created by Peter on 2/26/2018.
 */
class RetrofitConfig(
        appInfoProviderLazy: () -> AppInfoProvider,
        languageProviderLazy: () -> LanguageProvider,
        userProfileInteractorLazy: () -> UserProfileInteractor,
        gsonLazy: () -> Gson) {

    private val appInfoProvider: AppInfoProvider by lazy(appInfoProviderLazy)
    private val languageProvider: LanguageProvider by lazy(languageProviderLazy)
    private val userProfileInteractor: UserProfileInteractor by lazy(userProfileInteractorLazy)
    private val gson: Gson by lazy(gsonLazy)


    fun makeRetrofit(): Retrofit {
        val baseUrl = appInfoProvider.endpoint

        val okHttpBuilder = OkHttpClient.Builder()
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)

        if (appInfoProvider.isDebug) {
            okHttpBuilder.addNetworkInterceptor(StethoInterceptor())

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(loggingInterceptor)
        }

        okHttpBuilder.addInterceptor { chain ->

            val request = chain.request()
            val newRequest: Request.Builder = request.newBuilder()

            newRequest.addHeader(NetworkContract.Base.LANGUAGE_HEADER, languageProvider.language)

            runBlocking {
                userProfileInteractor.getToken()?.let {
                    newRequest.addHeader(
                            NetworkContract.Base.AUTHORIZATION_HEADER,
                            NetworkContract.Base.AUTHORIZATION_HEADER_VALUE_MASK.format(it))
                }
                userProfileInteractor.getAccountIdOrNull()?.let { newRequest.addHeader(NetworkContract.Base.ACCOUNT_ID_HEADER, it) }
            }


            val response = chain.proceed(newRequest.build())

            response

        }


        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))

        return retrofitBuilder.client(okHttpBuilder.build()).build()
    }

}