package com.mnassa.core

import android.os.Bundle

/**
 * Created by Peter on 2/20/2018.
 */
interface BaseViewModel {
    fun onCreate(savedInstanceState: Bundle?)
    fun saveInstanceState(outBundle: Bundle)
    fun onCleared()
}