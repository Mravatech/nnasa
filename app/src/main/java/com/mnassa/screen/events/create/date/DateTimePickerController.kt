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
import com.mnassa.extensions.formatAsDate
import com.mnassa.extensions.formatAsDateTime
import com.mnassa.extensions.formatAsDuration
import com.mnassa.extensions.formatAsTime
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_event_date_time.view.*
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog
import org.kodein.di.generic.instance
import java.io.Serializable
import java.util.*

/**
 * Created by Peter on 4/25/2018.
 */
class DateTimePickerController(args: Bundle) : MnassaControllerImpl<DateTimePickerViewModel>(args), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, TimeDurationPickerDialog.OnDurationSetListener {
    override val layoutId: Int = R.layout.controller_event_date_time
    override val viewModel: DateTimePickerViewModel by instance()
    private val dialogHelper: DialogHelper by instance()
    private val startDateTime = Calendar.getInstance()
    private var durationMillis: Long = 0L

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        val resultListener = targetController as OnDatePickerResultListener

        with(view) {
            toolbar.withActionButton(fromDictionary(R.string.event_date_time_button)) {
                resultListener.onDateTimeResult(DatePickerResult(startDateTime.time, durationMillis))
                close()
            }
            tilDate.hint = fromDictionary(R.string.event_date_placeholder)
            tilTime.hint = fromDictionary(R.string.event_time_placeholder)
            tilDuration.hint = fromDictionary(R.string.event_duration_placeholder)

            etDate.setOnClickListener {
                dialogHelper.calendarDialogFuture(it.context, this@DateTimePickerController, startDateTime)
            }
            etTime.setOnClickListener {
                dialogHelper.timeDialog(it.context, this@DateTimePickerController, startDateTime)
            }
            etDuration.setOnClickListener {
                dialogHelper.durationDialog(it.context, this@DateTimePickerController, durationMillis)
            }

            if (args.containsKey(EXTRA_INIT_DATA)) {
                val initData = args[EXTRA_INIT_DATA] as DatePickerResult
                args.remove(EXTRA_INIT_DATA)
                val calendar = Calendar.getInstance()
                calendar.time = initData.startDateTime
                onDateSet(null, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
                onTimeSet(null, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
                onDurationSet(null, initData.durationMillis)
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        startDateTime.set(Calendar.YEAR, year)
        startDateTime.set(Calendar.MONTH, month)
        startDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        launchCoroutineUI {
            with(getViewSuspend()) {
                etDate.setText(startDateTime.time.formatAsDate())
                validateInput()
            }
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
        startDateTime.set(Calendar.MINUTE, minute)
        launchCoroutineUI {
            with(getViewSuspend()) {
                etTime.setText(startDateTime.time.formatAsTime())
                validateInput()
            }
        }
    }

    override fun onDurationSet(view: TimeDurationPicker?, duration: Long) {
        durationMillis = duration
        launchCoroutineUI {
            with(getViewSuspend()) {
                etDuration.setText(duration.formatAsDuration())
                validateInput()
            }
        }
    }

    private fun validateInput() {
        launchCoroutineUI {
            getViewSuspend().toolbar.actionButtonClickable = durationMillis > 0L &&
                    startDateTime.timeInMillis > System.currentTimeMillis()
        }
    }

    interface OnDatePickerResultListener {
        fun onDateTimeResult(result: DatePickerResult)
    }

    data class DatePickerResult(
            val startDateTime: Date,
            val durationMillis: Long
    ) : Serializable {
        fun format(): CharSequence = "${startDateTime.formatAsDateTime()} | ${durationMillis.formatAsDuration()}"
    }

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