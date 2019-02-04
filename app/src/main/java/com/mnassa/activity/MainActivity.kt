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
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate
import com.mnassa.core.addons.launchCoroutineUI
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
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay


open class MainActivity : BaseActivity(), MnassaRouter by MnassaRouterDelegate(), SubscriptionContainer by SubscriptionsContainerDelegate() {

    private lateinit var router: Router
    private lateinit var onLogoutListener: (LogoutReason) -> Unit
    private val balanceChangeReceiver = BalanceChangeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
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

        launchCoroutineUI {
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
