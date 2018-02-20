package com.mnassa

import android.app.Application
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.autoAndroidModule
import com.github.salomonbrys.kodein.lazy
import com.mnassa.di.viewModelsModule

/**
 * Created by Peter on 2/20/2018.
 */
class App : Application(), KodeinAware {
    override val kodein: Kodein by Kodein.lazy {
        import(autoAndroidModule(this@App))
        import(viewModelsModule)
    }
}