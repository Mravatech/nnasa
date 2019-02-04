package com.mnassa.core

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.mnassa.core.addons.SubscriptionContainer
import com.mnassa.core.addons.SubscriptionsContainerDelegate

/**
 * Created by Peter on 2/20/2018.
 */
abstract class BaseViewModelImpl : ViewModel(), BaseViewModel, SubscriptionContainer by SubscriptionsContainerDelegate() {

    override fun onCreate(savedInstanceState: Bundle?) {

    }

    override fun saveInstanceState(outBundle: Bundle) {

    }

    @CallSuper
    final override fun onCleared() {
        super.onCleared()
        cancelAllSubscriptions()
    }
}