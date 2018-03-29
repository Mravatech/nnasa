package com.mnassa.activity

import android.app.Activity
import android.app.FragmentManager
import android.app.LoaderManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.*
import com.github.salomonbrys.kodein.bindings.InstanceBinding
import com.mnassa.R
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.MnassaRouterDelegate
import com.mnassa.screen.splash.SplashController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AndroidInjector<Activity, AndroidScope<Activity>>, MnassaRouter by MnassaRouterDelegate() {
    override val kodeinScope: AndroidScope<Activity> = androidActivityScope
    override fun initializeInjector() {
        val activityModule = Kodein.Module {
            Bind<KodeinInjected>(erased()) with InstanceBinding(erased(), this@MainActivity)
            Bind<Activity>(erased()) with InstanceBinding(erased(), kodeinComponent)
            Bind<FragmentManager>(erased()) with InstanceBinding(erased(), kodeinComponent.fragmentManager)
            Bind<LoaderManager>(erased()) with InstanceBinding(erased(), kodeinComponent.loaderManager)
            Bind<LayoutInflater>(erased(), tag = ACTIVITY_LAYOUT_INFLATER) with InstanceBinding(erased(), kodeinComponent.layoutInflater)

            import(provideOverridingModule(), allowOverride = true)
        }

        val kodein = Kodein {
            extend(kodeinComponent.appKodein(), allowOverride = true)
            import(activityModule, allowOverride = true)
        }

        injector.inject(kodein)
    }

    override val injector = KodeinInjector()
    private lateinit var router: Router
    private lateinit var onLogoutListener: (Unit) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        initializeInjector()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(SplashController.newInstance()))
        }

        onLogoutListener = instance<LoginInteractor>().value.onLogoutListener.subscribe {
            router.popToRoot()
            router.replaceTopController(RouterTransaction.with(SplashController.newInstance()))
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        instance<LoginInteractor>().value.onLogoutListener.unSubscribe(onLogoutListener)
        destroyInjector()
        super.onDestroy()

    }
}
