package com.mnassa.screen.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Peter on 4/5/2018.
 */
class BaseAdapter {
    val headerItems: List<AdapterItem> = ArrayList()
    val mediumItems: List<AdapterItem> = ArrayList()
    val footerItems: List<AdapterItem> = ArrayList()
}

interface AdapterItemType

interface AdapterItem {
//    val data: Any
    val type: AdapterItemType

    fun isContentTheSame(other: AdapterItem)
    fun isItemTheSame(other: AdapterItem)
    fun inflate(parent: ViewGroup, layoutInflater: LayoutInflater): View
    fun bind(view: View)
}
