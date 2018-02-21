package com.mnassa.data.network

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.singleton
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.mnassa.data.network.api.UsersApi
import com.mnassa.domain.other.AppInfoProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Peter on 2/21/2018.
 */
val networkModule = Kodein.Module {

    bind<Retrofit>() with singleton {
        val appInfoProvider: AppInfoProvider = instance()
        val baseUrl = appInfoProvider.endpoint

        Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    bind<UsersApi>() with singleton {
        val retrofit: Retrofit = instance()
        retrofit.create(UsersApi::class.java)
    }

}