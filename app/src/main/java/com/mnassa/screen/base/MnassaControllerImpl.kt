package com.mnassa.screen.base

import android.content.Context
import android.os.Bundle
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.mnassa.core.BaseControllerImpl
import com.mnassa.core.BaseViewModel
import java.util.*

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaControllerImpl<VM : BaseViewModel> : BaseControllerImpl<VM>(), MnassaController<VM>, AndroidInjector<MnassaController<VM>, AndroidScope<MnassaController<VM>>> {
    final override val injector = KodeinInjector()
    final override val kodeinComponent = super.kodeinComponent
    final override val kodeinScope: AndroidScope<MnassaController<VM>> = object : AndroidScope<MnassaController<VM>> {
        override fun getRegistry(context: MnassaController<VM>): ScopeRegistry = synchronized(CONTEXT_SCOPES) { CONTEXT_SCOPES.getOrPut(context) { ScopeRegistry() } }
        override fun removeFromScope(context: MnassaController<VM>): ScopeRegistry? = CONTEXT_SCOPES.remove(context)
    }

    override fun initializeInjector() {
        val activityModule = Kodein.Module {
            Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@MnassaControllerImpl)
            Bind<Context>(erased()) with InstanceBinding(erased(), requireNotNull(applicationContext))
            import(provideOverridingModule(), allowOverride = true)
        }

        val kodein = Kodein {
            extend(requireNotNull(applicationContext).appKodein(), allowOverride = true)
            import(activityModule, allowOverride = true)
        }

        injector.inject(kodein)
    }

    override fun provideOverridingModule() = Kodein.Module {

    }

    override fun onCreated(savedInstanceState: Bundle?) {
        initializeInjector()
        super.onCreated(savedInstanceState)
    }

    private companion object {
        private val CONTEXT_SCOPES = WeakHashMap<MnassaController<*>, ScopeRegistry>()
    }
}

