package com.mnassa.screen.base

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.github.salomonbrys.kodein.erased
import com.mnassa.App
import com.mnassa.core.BaseViewModelImpl
import java.util.*

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaViewModelImpl : BaseViewModelImpl(), MnassaViewModel, AndroidInjector<MnassaViewModel, AndroidScope<MnassaViewModel>> {
    final override val injector = KodeinInjector()
    final override val kodeinComponent = super.kodeinComponent
    final override val kodeinScope: AndroidScope<MnassaViewModel> = object : AndroidScope<MnassaViewModel> {
        override fun getRegistry(context: MnassaViewModel): ScopeRegistry = synchronized(CONTEXT_SCOPES) { CONTEXT_SCOPES.getOrPut(context) { ScopeRegistry() } }
        override fun removeFromScope(context: MnassaViewModel): ScopeRegistry? = CONTEXT_SCOPES.remove(context)
    }

    override fun initializeInjector() {
        val activityModule = Kodein.Module {
            Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@MnassaViewModelImpl)
            Bind<Context>(erased()) with InstanceBinding(erased(), requireNotNull(App.context))
            import(provideOverridingModule(), allowOverride = true)
        }

        val kodein = Kodein {
            extend(App.context.appKodein(), allowOverride = true)
            import(activityModule, allowOverride = true)
        }

        injector.inject(kodein)
    }

    override fun provideOverridingModule() = Kodein.Module {

    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        initializeInjector()
        super.onCreate(savedInstanceState)
    }

    private companion object {
        private val CONTEXT_SCOPES = WeakHashMap<MnassaViewModel, ScopeRegistry>()
    }
}