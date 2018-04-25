package com.mnassa.screen.events.create.date

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_date_time.view.*
import org.kodein.di.generic.instance
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Peter on 4/25/2018.
 */
class DateTimePickerController(args: Bundle) : MnassaControllerImpl<DateTimePickerViewModel>(args), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    override val layoutId: Int = R.layout.controller_event_date_time
    override val viewModel: DateTimePickerViewModel by instance()
    private val dialogHelper: DialogHelper by instance()
    private val languageProvider: LanguageProvider by instance()
    private val startDateTime = Calendar.getInstance()
    private var durationMillis: Long = 0L
    private val dateFormatter by lazy { SimpleDateFormat("dd MMM yyyy", languageProvider.locale) }
    private val timeFormatter by lazy { SimpleDateFormat("hh:mm a", languageProvider.locale) }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        val resultListener = targetController as OnDatePickerResultListener

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.event_date_time_button)) {
                resultListener.onResult(DatePickerResult(startDateTime.time, durationMillis))
            }
            tilDate.hint = fromDictionary(R.string.event_date_placeholder)
            tilTime.hint = fromDictionary(R.string.event_time_placeholder)
            tilDuration.hint = fromDictionary(R.string.event_duration_placeholder)

            etDate.setOnClickListener {
                dialogHelper.calendarDialog(it.context, this@DateTimePickerController, startDateTime)
            }
            etTime.setOnClickListener {
                dialogHelper.timeDialog(it.context, this@DateTimePickerController, startDateTime)
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        startDateTime.set(Calendar.YEAR, year)
        startDateTime.set(Calendar.MONTH, month)
        startDateTime.set(Calendar.DAY_OF_MONTH, month)
        launchCoroutineUI {
            with(getViewSuspend()) {
                etDate.setText(dateFormatter.format(startDateTime.timeInMillis))
            }
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
        startDateTime.set(Calendar.MINUTE, minute)
        launchCoroutineUI {
            with(getViewSuspend()) {
                etTime.setText(timeFormatter.format(startDateTime.timeInMillis))
            }
        }
    }

    interface OnDatePickerResultListener {
        fun onResult(result: DatePickerResult)
    }

    data class DatePickerResult(
            val startDateTime: Date,
            val durationMillis: Long
    ) : Serializable

    companion object {
        private const val EXTRA_INIT_DATA = "EXTRA_INIT_DATA"

        fun <T> newInstance(listener: T, initData: DatePickerResult? = null): DateTimePickerController where T : OnDatePickerResultListener, T : Controller {
            val args = Bundle()
            initData?.apply { args.putSerializable(EXTRA_INIT_DATA, this) }

            val controller = DateTimePickerController(args)
            controller.targetController = listener
            return controller
        }
    }
}