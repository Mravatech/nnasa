package com.mnassa.screen.events.details

import android.os.Bundle
import com.mnassa.data.network.NetworkContract
import com.mnassa.domain.interactor.ComplaintInteractor
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.EventStatus
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.ComplaintModelImpl
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                if (it != null) {
                    eventChannel.send(it)
                } else {
                    //TODO
                }
            }
        }
        resolveExceptions {
            reportsList = complaintInteractor.getReports()
        }
    }

    override fun changeStatus(event: EventModel, status: EventStatus) {
        resolveExceptions {
            withProgressSuspend {
                eventsInteractor.changeStatus(event, status)
            }
        }
    }

    override fun sendComplaint(eventId: String, reason: String, authorText: String?) {
        resolveExceptions {
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
        resolveExceptions {
            withProgressSuspend {
                eventsInteractor.promote(eventId)
            }
        }
    }
}