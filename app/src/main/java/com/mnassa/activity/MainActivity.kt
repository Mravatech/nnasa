package com.mnassa.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.MnassaRouterDelegate
import com.mnassa.screen.splash.SplashController
import com.mnassa.service.MnassaFirebaseMessagingService
import com.mnassa.translation.LanguageProviderImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.in_out_come_toast.view.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.KodeinContext
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import java.util.*
import android.app.NotificationManager
import android.support.v7.app.AppCompatDelegate
import com.mnassa.core.addons.*


open class MainActivity : AppCompatActivity(), KodeinAware, MnassaRouter by MnassaRouterDelegate(), SubscriptionContainer by SubscriptionsContainerDelegate() {

    @Suppress("LeakingThis")
    override val kodeinContext: KodeinContext<*> = kcontext(this)
    private val _parentKodein by closestKodein()
    override val kodein: Kodein by retainedKodein {
        extend(_parentKodein, allowOverride = true)
    }

    private lateinit var router: Router
    private lateinit var onLogoutListener: (LogoutReason) -> Unit
    private val balanceChangeReceiver = BalanceChangeReceiver()

    private val languageProvider: LanguageProvider by instance()
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(LanguageProviderImpl.LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
        val lang = prefs.getString(LanguageProviderImpl.LANGUAGE_SETTINGS, null)
        lang?.let {
            languageProvider.locale = Locale(it)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(createRootControllerInstance()))
        }

        onLogoutListener = getInstance<LoginInteractor>().onLogoutListener.subscribe { reason ->
            when (reason) {
                is LogoutReason.NotAuthorized -> {
                }
                is LogoutReason.ManualLogout -> {
                }
                is LogoutReason.AccountBlocked, is LogoutReason.UserBlocked -> {
                    Toast.makeText(applicationContext, fromDictionary(R.string.blocked_account_message), Toast.LENGTH_LONG).show()
                }
            }
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

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MnassaFirebaseMessagingService.NOTIFICATION)
        registerReceiver(balanceChangeReceiver, intentFilter)

        //close all push-notifications
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onPause() {
        unregisterReceiver(balanceChangeReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        getInstance<LoginInteractor>().onLogoutListener.unSubscribe(onLogoutListener)
        cancelAllSubscriptions()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        SavedInstanceFragment.getInstance(fragmentManager).pushData(outState.clone() as Bundle)
        // advise was taken from here = https://www.devsbedevin.com/avoiding-transactiontoolargeexception-on-android-nougat-and-up/
        outState.clear() // We don't want a TransactionTooLargeException, so we handle things via the SavedInstanceFragment
    }

    private class BalanceChangeReceiver : BroadcastReceiver() {
        private var notificationsQueue: Deferred<Unit>? = null

        override fun onReceive(context: Context, intent: Intent) {
            launchUI {
                notificationsQueue?.await()
                notificationsQueue = async(UI) {
                    val fromName = intent.getStringExtra(MnassaFirebaseMessagingService.FROM_USER)
                    val amount = intent.getStringExtra(MnassaFirebaseMessagingService.AMOUNT).toLong()
                    if (amount == 0L) return@async
                    val layout = View.inflate(context, R.layout.in_out_come_toast, null)
                    layout.iocView.showView(amount, fromName)
                    val toast = Toast(context.applicationContext)
                    toast.setGravity(Gravity.FILL, START_OFFSET, START_OFFSET)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view = layout
                    toast.show()

                    delay(LONG_TOAST_DURATION_MILLIS)
                }
            }
        }
    }

    companion object {
        private const val START_OFFSET = 0
        private const val LONG_TOAST_DURATION_MILLIS = 4_500

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

}
