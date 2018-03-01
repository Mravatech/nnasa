package com.mnassa.screen.base

import android.os.Bundle
import android.view.View
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.mnassa.R
import com.mnassa.core.BaseControllerImpl
import com.mnassa.core.BaseViewModel
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.progress.MnassaProgressDialog
import kotlinx.coroutines.experimental.channels.consumeEach
import java.util.*

/**
 * Created by Peter on 2/20/2018.
 */
abstract class MnassaControllerImpl<VM : MnassaViewModel> : BaseControllerImpl<VM>, MnassaController<VM>, AndroidInjector<MnassaController<VM>, AndroidScope<MnassaController<VM>>> {
    final override val injector = KodeinInjector()
    final override val kodeinComponent = super.kodeinComponent
    final override val kodeinScope: AndroidScope<MnassaController<VM>> = object : AndroidScope<MnassaController<VM>> {
        override fun getRegistry(context: MnassaController<VM>): ScopeRegistry = synchronized(CONTEXT_SCOPES) { CONTEXT_SCOPES.getOrPut(context) { ScopeRegistry() } }
        override fun removeFromScope(context: MnassaController<VM>): ScopeRegistry? = CONTEXT_SCOPES.remove(context)
    }

    constructor(params: Bundle): super(params)
    constructor(): super()

    override fun initializeInjector() {
        val activityModule = Kodein.Module {
            Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@MnassaControllerImpl)
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        subscribeToProgressEvents()
    }

    override fun onViewDestroyed(view: View) {
        hideProgress() //prevent showing progress after screen change
        super.onViewDestroyed(view)
    }

    protected open fun subscribeToProgressEvents() {
        launchCoroutineUI {
            viewModel.isProgressEnabledChannel.consumeEach { isProgressEnabled ->
                if (isProgressEnabled) showProgress() else hideProgress()
            }
        }
    }

    private var progressDialog: MnassaProgressDialog? = null
    protected fun showProgress() {
        if (progressDialog != null) return

        progressDialog?.cancel()

        val dialog = MnassaProgressDialog(requireNotNull(view).context, R.style.MnassaProgressTheme)
        dialog.setCancelable(false)
        dialog.show()

        progressDialog = dialog
    }

    protected fun hideProgress() {
        progressDialog?.cancel()
        progressDialog = null
    }

    private companion object {
        private val CONTEXT_SCOPES = WeakHashMap<MnassaController<*>, ScopeRegistry>()
    }
}

