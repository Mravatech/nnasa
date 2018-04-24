package com.mnassa.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Toast
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.MnassaRouterDelegate
import com.mnassa.screen.splash.SplashController
import com.mnassa.service.MnassaFirebaseMessagingService
import com.mnassa.translation.LanguageProviderImpl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.in_out_come_toast.view.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinContext
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import java.util.*

open class MainActivity : AppCompatActivity(), KodeinAware, MnassaRouter by MnassaRouterDelegate() {

    @Suppress("LeakingThis")
    override val kodeinContext: KodeinContext<*> = kcontext(this)
    private val _parentKodein by closestKodein()
    override val kodein: Kodein by retainedKodein {
        extend(_parentKodein, allowOverride = true)
    }

    private lateinit var router: Router
    private lateinit var onLogoutListener: (Unit) -> Unit

    private val languageProvider: LanguageProvider by instance()
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
        val prefs = getSharedPreferences(LanguageProviderImpl.LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
        val lang = prefs.getString(LanguageProviderImpl.LANGUAGE_SETTINGS, null)
        lang?.let {
            languageProvider.locale = Locale(it)
        }
    }

    protected open fun createRootControllerInstance(): Controller = SplashController.newInstance()

    override fun onBackPressed() {
        hideKeyboard()
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MnassaFirebaseMessagingService.NOTIFICATION)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        getInstance<LoginInteractor>().onLogoutListener.unSubscribe(onLogoutListener)
        super.onDestroy()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val fromName = intent.getStringExtra(MnassaFirebaseMessagingService.FROM_USER)
            val amount = intent.getStringExtra(MnassaFirebaseMessagingService.AMOUNT).toInt()
            val layout = layoutInflater.inflate(R.layout.in_out_come_toast, findViewById(R.id.toastRoot))
            layout.iocView.showView(amount, fromName)
            val toast = Toast(applicationContext)
            toast.setGravity(Gravity.FILL, START_OFFSET, START_OFFSET)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layout
            val toastCountDown: CountDownTimer
            toastCountDown = object : CountDownTimer(COINS_ANIMATION_TOAST_DURATION, COUNT_DOWN_INTERVAL /*Tick duration*/) {
                override fun onTick(millisUntilFinished: Long) {
                    toast.show()
                }

                override fun onFinish() {
                    toast.cancel()
                }
            }
            // Show the toast and starts the countdown
            toast.show()
            toastCountDown.start()
        }
    }

    companion object {
        const val COINS_ANIMATION_TOAST_DURATION = 6000L
        const val COUNT_DOWN_INTERVAL = 1000L
        const val START_OFFSET = 0
    }

}
