package com.mnassa.screen.base

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.AndroidInjector
import com.github.salomonbrys.kodein.android.AndroidScope
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.github.salomonbrys.kodein.bindings.ScopeRegistry
import com.github.salomonbrys.kodein.erased
import com.mnassa.R
import com.mnassa.core.BaseControllerImpl
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.progress.MnassaProgressDialog
import com.mnassa.translation.fromDictionary
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

    constructor(params: Bundle) : super(params)
    constructor() : super()

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
        subscribeToErrorEvents()
    }

    override fun onViewDestroyed(view: View) {
        hideProgress() //prevent showing progress after screen change
        hideKeyboard()
        super.onViewDestroyed(view)
    }

    protected open fun subscribeToErrorEvents() {
        launchCoroutineUI {
            viewModel.errorMessageChannel.consumeEach {
                val context = view?.context ?: return@consumeEach
                AlertDialog.Builder(context)
                        .setTitle(fromDictionary(R.string.error_dialog_title))
                        .setMessage(it)
                        .setPositiveButton(context.getString(android.R.string.ok), { _, _ -> })
                        .show()
            }
        }
    }

    protected open fun subscribeToProgressEvents() {
        launchCoroutineUI {
            viewModel.isProgressEnabledChannel.consumeEach { isProgressEnabled ->
                if (isProgressEnabled) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }
        }
    }

    private var progressDialog: MnassaProgressDialog? = null
    protected fun showProgress() {
        hideKeyboard()

        if (progressDialog != null) return

        progressDialog?.cancel()

        val dialog = MnassaProgressDialog(requireNotNull(view).context, R.style.MnassaProgressTheme)
        dialog.setCancelable(false)
        dialog.show()

        progressDialog = dialog
    }

    protected fun hideProgress() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    protected fun open(controller: Controller) = mnassaRouter.open(this, controller)
    protected fun close() = mnassaRouter.close(this)

    protected val mnassaRouter: MnassaRouter
        get() {
            val parentController = parentController
            val activity = activity

            return when {
                parentController is MnassaRouter -> parentController
                activity is MnassaRouter -> activity
                else -> throw IllegalStateException("Mnassa router not found for $this")
            }
        }

    private companion object {
        private val CONTEXT_SCOPES = WeakHashMap<MnassaController<*>, ScopeRegistry>()
    }
}

