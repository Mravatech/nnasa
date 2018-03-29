package com.mnassa.module

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.StorageReference
import com.mnassa.domain.other.AppInfoProvider
import okhttp3.Call
import okhttp3.OkHttpClient
import java.io.InputStream


@Suppress("unused")
@com.bumptech.glide.annotation.GlideModule
class MnassaGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference::class.java, InputStream::class.java, FirebaseImageLoader.Factory())

        val builder = OkHttpClient.Builder()
        if (context.appKodein().instance<AppInfoProvider>().isDebug) {
            builder.addNetworkInterceptor(StethoInterceptor())
        }
        val client = builder.build()
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
        registry.replace(String::class.java, InputStream::class.java, OkHttpStringUrlLoader.Factory(client))
    }

    //======================================= ABSTRACT FACTORY =========================================

    internal abstract class AbstractFactory<IN>(protected val client: Call.Factory) : ModelLoaderFactory<IN, InputStream> {

        abstract override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<IN, InputStream>

        override fun teardown() {
            // Do nothing, this instance doesn't own the client.
        }
    }

//====================================== STRING URL LOADER =========================================

    internal class OkHttpStringUrlLoader private constructor(private val client: Call.Factory) : ModelLoader<String, InputStream> {
        private val wrongUrl = "http://wrong.url/i.jpg"

        override fun handles(url: String): Boolean = true

        override fun buildLoadData(model: String, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
            val glideUrl: GlideUrl = when {
                model.isBlank() -> GlideUrl(wrongUrl)
                else -> GlideUrl(model)
            }

            return ModelLoader.LoadData(glideUrl, OkHttpStreamFetcher(client, glideUrl))
        }

        internal class Factory(client: Call.Factory) : AbstractFactory<String>(client) {

            override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
                return OkHttpStringUrlLoader(client)
            }
        }
    }

//======================================= GLIDE URL LOADER =========================================

    internal class OkHttpUrlLoader private constructor(private val client: Call.Factory) : ModelLoader<GlideUrl, InputStream> {
        override fun handles(url: GlideUrl): Boolean = true

        override fun buildLoadData(model: GlideUrl, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
            return ModelLoader.LoadData(model, OkHttpStreamFetcher(client, model))
        }

        internal class Factory(client: Call.Factory) : AbstractFactory<GlideUrl>(client) {
            override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideUrl, InputStream> = OkHttpUrlLoader(client)
        }
    }
}

