package com.mnassa.widget.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.impl.AccountAbilityImpl
import com.mnassa.screen.accountinfo.personal.PersonalInfoController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.selectable_fake_edit_text.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/16/2018
 */

class AbilitySelectableEditText : LinearLayout {

    private val dialogHelper = context.appKodein().instance<DialogHelper>()
    private var occupationPositionInList = -1

    private var isFirst: Boolean = true
    lateinit var addNewSelectableViewListener: AddNewSelectableViewListener

    constructor(context: Context, isMain: Boolean = false, listener: AddNewSelectableViewListener) : super(context) {
        isFirst = isMain
        addNewSelectableViewListener = listener
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.selectable_fake_edit_text, this)
        tvSelectView.text = fromDictionary(R.string.reg_occupation_edit_text_place_holder)
        tilWorkAt.hint = fromDictionary(R.string.invite_at_placeholder)
        tilCustomOccupation.hint = fromDictionary(R.string.reg_dialog_custom_occupation)
        val occupations = listOf(fromDictionary(R.string.reg_dialog_student),
                fromDictionary(R.string.reg_dialog_housewife),
                fromDictionary(R.string.reg_dialog_employee),
                fromDictionary(R.string.reg_dialog_business_owner),
                fromDictionary(R.string.reg_dialog_other)
        )
        tvSelectView.setOnClickListener {
            dialogHelper.showChooseOccupationDialog(context, occupations, occupationPositionInList, {
                tvSelectLabel.visibility = View.VISIBLE
                tvSelectView.text = occupations[it]
                tilWorkAt.visibility = View.VISIBLE
                occupationPositionInList = it
                tilCustomOccupation.visibility = if (it == PersonalInfoController.OTHER) View.VISIBLE else View.GONE
                addNewSelectableViewListener.addSelectableViewIsAvailable(true)
            })
        }
    }

    fun getAbility(): AccountAbility {
        return AccountAbilityImpl(isFirst, etCustomOccupation.text.toString(), etWorkAt.text.toString())
    }

    interface AddNewSelectableViewListener {
        fun addSelectableViewIsAvailable(isAvailable: Boolean)
    }

}