package com.mnassa.screen.hail.history

import com.mnassa.domain.model.PhoneContact
import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */

interface HistoryViewModel : MnassaViewModel {
    val phoneContactChannel: BroadcastChannel<List<PhoneContactInvited>>
    fun retrievePhoneContacts()
}