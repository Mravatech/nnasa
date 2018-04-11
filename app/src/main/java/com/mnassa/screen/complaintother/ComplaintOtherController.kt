package com.mnassa.screen.complaintother

import android.view.View
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_complaint_other.view.*
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
class ComplaintOtherController : MnassaControllerImpl<ComplaintOtherViewModel>() {

    override val layoutId: Int = R.layout.controller_complaint_other
    override val viewModel: ComplaintOtherViewModel by instance()
    private val resultListener by lazy { targetController as OnComplaintResult }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tvInfoComplaint.text = fromDictionary(R.string.complaint_describe_reason)
        view.tilComplaintMessage.hint = fromDictionary(R.string.complaint_type_message)
        view.tlbrComplaint.actionButtonClickable = true
        view.tlbrComplaint.withActionButton(fromDictionary(R.string.complaint_send), {
            resultListener.onComplaint = view.etComplaintMessage.text.toString()
            close()
        })
    }


    interface OnComplaintResult {
        var onComplaint: String
    }

    companion object {
        fun newInstance() = ComplaintOtherController()
    }

}