package com.mnassa.activity

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.di.getInstance
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.domain.model.LogoutReason
import com.mnassa.extensions.hideKeyboard
import com.mnassa.screen.MnassaRouter
import com.mnassa.screen.MnassaRouterDelegate
import com.mnassa.screen.splash.SplashController
import com.mnassa.service.MnassaFirebaseMessagingService
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.in_out_come_toast.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class MainActivity : BaseActivity(), MnassaRouter by MnassaRouterDelegate() {

    private lateinit var router: Router

    private val balanceChangeReceiver = BalanceChangeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = Conductor.attachRouter(this, container, savedInstanceState)
            .apply {
                if (hasRootController()) {
                } else {
                    val root = createRootControllerInstance()
                    setRoot(RouterTransaction.with(root))
                }
            }

        launchUI {
            getInstance<LoginInteractor>().onLogoutListener
                .openSubscription()
                .consumeEach { reason ->
                    when (reason) {
                        is LogoutReason.AccountBlocked,
                        is LogoutReason.UserBlocked -> {
                            Toast.makeText(
                                applicationContext,
                                fromDictionary(R.string.blocked_account_message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    switchToMainActivity()
                }
        }

        launchUI {
            getInstance<SettingsInteractor>()
                .getMaintenanceServerStatus()
                .consumeEach(::processMaintenanceStatusChange)
        }
    }

    protected open fun createRootControllerInstance(): Controller = SplashController.newInstance()

    protected open fun processMaintenanceStatusChange(isMaintenance: Boolean) {
        if (isMaintenance) {
            switchToMaintenanceActivity()
        }
    }

    private fun switchToMaintenanceActivity() {
        Intent(this, MaintenanceActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let(::startActivity)
        overridePendingTransition(0, 0)
    }

    private fun switchToMainActivity() {
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .let(::startActivity)
    }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // advise was taken from here = https://www.devsbedevin.com/avoiding-transactiontoolargeexception-on-android-nougat-and-up/
        outState.clear() // We don't want a TransactionTooLargeException, so we handle things via the SavedInstanceFragment
    }

    private class BalanceChangeReceiver : BroadcastReceiver() {
        private val mutex = Mutex()

        override fun onReceive(context: Context, intent: Intent) {
            GlobalScope.launchUI {
                mutex.withLock {
                    val fromName = intent.getStringExtra(MnassaFirebaseMessagingService.FROM_USER)
                    val amount = intent.getStringExtra(MnassaFirebaseMessagingService.AMOUNT)
                        .toLongOrNull()
                        ?.takeUnless { it == 0L }
                        ?: return@withLock

                    Toast(context.applicationContext).apply {
                        setGravity(Gravity.FILL, START_OFFSET, START_OFFSET)
                        duration = Toast.LENGTH_LONG
                        view = View.inflate(context, R.layout.in_out_come_toast, null).apply {
                            iocView.showView(amount, fromName)
                        }
                    }.show()

                    delay(LONG_TOAST_DURATION_MILLIS)
                }
            }
        }
    }

    companion object {
        private const val START_OFFSET = 0
        private const val LONG_TOAST_DURATION_MILLIS = 4_500L

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

}
