package com.mnassa.screen.base.adapter.new

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Peter on 4/6/2018.
 */
interface AdapterItem : Comparable<AdapterItem> {
    val viewType: AdapterItemViewType<AdapterItem>

    fun isContentTheSame(other: AdapterItem): Boolean
    fun isItemTheSame(other: AdapterItem): Boolean
    override fun compareTo(other: AdapterItem): Int
}

interface AdapterItemViewType<in Item: AdapterItem> {
    val id: Int
    fun inflate(parent: ViewGroup, layoutInflater: LayoutInflater, onClickListener: View.OnClickListener): View
    fun bind(view: View, item: Item)
}