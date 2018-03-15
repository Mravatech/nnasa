package com.mnassa.screen.needs

import android.os.Bundle
import com.mnassa.domain.model.NewsFeedItemModel
import com.mnassa.domain.repository.NewsFeedRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 3/6/2018.
 */
class NeedsViewModelImpl(private val newsFeedRepository: NewsFeedRepository) : MnassaViewModelImpl(), NeedsViewModel {

    override suspend fun getNewsFeedChannel(): ReceiveChannel<NewsFeedItemModel> = newsFeedRepository.loadAll()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}