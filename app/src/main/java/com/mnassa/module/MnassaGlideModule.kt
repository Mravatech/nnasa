package com.mnassa.module

import android.content.Context
import com.bumptech.glide.Registry
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.InputStream
import com.bumptech.glide.Glide

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */
@SuppressWarnings("unused")
@com.bumptech.glide.annotation.GlideModule
class MnassaGlideModule : AppGlideModule(){

    override fun registerComponents(context: Context?, glide: Glide?, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference::class.java, InputStream::class.java,
                FirebaseImageLoader.Factory())
    }

}