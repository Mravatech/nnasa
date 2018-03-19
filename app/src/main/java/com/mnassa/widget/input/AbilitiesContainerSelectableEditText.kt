package com.mnassa.widget.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mnassa.R
import com.mnassa.domain.model.AccountAbility
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.container_selectable_fake_edit_text.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class AbilitiesContainerSelectableEditText : LinearLayout, AbilitySelectableEditText.AddNewSelectableViewListener {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        orientation = VERTICAL
        inflate(context, R.layout.container_selectable_fake_edit_text, this)
        selectOccupation.addNewSelectableViewListener = this
        tvAddAnotherOccupation.text = fromDictionary(R.string.reg_add_another_occupation)
        tvAddAnotherOccupation.setOnClickListener {
            tvAddAnotherOccupation.visibility = View.GONE
            containerSelectable.addView(AbilitySelectableEditText(context, false, this), containerSelectable.childCount - 1)
        }
    }

    override fun addSelectableViewIsAvailable(isAvailable: Boolean) {
        tvAddAnotherOccupation.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    fun getAllAbilities():List<AccountAbility>{
        val abilities = mutableListOf<AccountAbility>()
        for (i in 0 until containerSelectable.childCount) {
            val view = containerSelectable.getChildAt(i)
            when(view){
                is AbilitySelectableEditText -> abilities.add(view.getAbility())
            }
        }
        return abilities
    }

}