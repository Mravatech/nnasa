package com.mnassa.screen.invite.history

import com.mnassa.domain.model.PhoneContactInvited
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

interface HistoryViewModel : MnassaViewModel {
    val phoneContactChannel: BroadcastChannel<List<PhoneContactInvited>>
}