package com.mnassa.screen.base.adapter

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/8/2018
 */


open class FilteredSortedDataStorage<ITEM>(private val filterPredicate: (item1: ITEM) -> Boolean,
                                           private val dataStorage: BasePaginationRVAdapter.DataStorage<ITEM>,
                                           private val adapter: BasePaginationRVAdapter<ITEM>) : BasePaginationRVAdapter.DataStorage<ITEM> by dataStorage, SearchListener {

    private var itemList: List<ITEM> = emptyList()

    override fun search() {
        if (itemList.isEmpty() || itemList.size < dataStorage.size){
            itemList = dataStorage.toList()
        }
        val newValues = itemList.filter(filterPredicate)
        dataStorage.set(newValues)
    }
}

interface SearchListener {
    fun search()
}