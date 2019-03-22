package com.mnassa.screen.events.details

import com.mnassa.core.addons.launchWorker
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventStatus
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 4/17/2018.
 */
class EventDetailsViewModelImpl(private val eventId: String,
                                private val eventsInteractor: EventsInteractor,
                                private val complaintInteractor: ComplaintInteractor) : MnassaViewModelImpl(), EventDetailsViewModel {
    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()
    override val finishScreenChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    private var reportsList = emptyList<TranslatedWordModel>()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                if (it != null) {
                    eventChannel.send(it)
                } else {
                    //TODO
                }
            }
        }
        setupScope.launchWorker {
            reportsList = complaintInteractor.getReports()
        }
    }

    override fun changeStatus(event: EventModel, status: EventStatus) {
        launchWorker {
            withProgressSuspend {
                eventsInteractor.changeStatus(event, status)
            }
        }
    }

    override fun sendComplaint(eventId: String, reason: String, authorText: String?) {
        launchWorker {
            withProgressSuspend {
                complaintInteractor.sendComplaint(ComplaintModelImpl(
                        id = eventId,
                        type = NetworkContract.Complaint.EVENT_TYPE,
                        reason = reason,
                        authorText = authorText
                ))
            }
            finishScreenChannel.send(Unit)
        }
    }

    override suspend fun retrieveComplaints(): List<TranslatedWordModel> {
        if (reportsList.isNotEmpty()) return reportsList
        showProgress()
        reportsList = complaintInteractor.getReports()
        hideProgress()
        return reportsList
    }

    override fun promote() {
        launchWorker {
            withProgressSuspend {
                eventsInteractor.promote(eventId)
            }
        }
    }
}