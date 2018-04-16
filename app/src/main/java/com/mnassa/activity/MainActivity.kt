package com.mnassa.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.MnassaRouterDelegate
import com.mnassa.screen.splash.SplashController
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinContext
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.kcontext

open class MainActivity : AppCompatActivity(), KodeinAware, MnassaRouter by MnassaRouterDelegate() {

    @Suppress("LeakingThis")
    override val kodeinContext: KodeinContext<*> = kcontext(this)
    private val _parentKodein by closestKodein()
    override val kodein: Kodein by retainedKodein {
        extend(_parentKodein, allowOverride = true)
    }

    private lateinit var router: Router
    private lateinit var onLogoutListener: (Unit) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(createRootControllerInstance()))
        }

        onLogoutListener = getInstance<LoginInteractor>().onLogoutListener.subscribe {
            startActivity(
                    Intent(this@MainActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }

    protected open fun createRootControllerInstance(): Controller = SplashController.newInstance()

    override fun onBackPressed() {
        hideKeyboard()
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        getInstance<LoginInteractor>().onLogoutListener.unSubscribe(onLogoutListener)
        super.onDestroy()
    }
}
