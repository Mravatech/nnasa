package com.mnassa.core

import android.arch.lifecycle.ViewModel

/**
 * Created by Peter on 2/20/2018.
 */
class BaseViewModelImpl : ViewModel(), BaseViewModel, SubscriptionContainer by SubscriptionsContainerDelegate() {

    override fun onCleared() {
        super.onCleared()
        cancelAllSubscriptions()
    }
}