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
            containerSelectable.addView(AbilitySelectableEditText(context, false, this), containerSelectable.childCount - PRE_LAST_POSITION)
        }
    }

    override fun addSelectableViewIsAvailable(isAvailable: Boolean) {
        tvAddAnotherOccupation.visibility = if (isAvailable) View.VISIBLE else View.GONE
    }

    fun getAllAbilities(): List<AccountAbility> {
        val abilities = mutableListOf<AccountAbility>()
        for (i in 0 until containerSelectable.childCount) {
            val view = containerSelectable.getChildAt(i)
            when (view) {
                is AbilitySelectableEditText -> {
                    val ability = view.getAbility()
                    if (requireNotNull(ability.name) != fromDictionary(R.string.reg_occupation_edit_text_place_holder)) {
                        abilities.add(ability)
                    }
                }
            }
        }
        return abilities
    }

    fun setAbilities(abilities: List<AccountAbility>) {
        if (abilities.isNotEmpty()) {
            for (i in 0..containerSelectable.childCount) {
                val childView = containerSelectable.getChildAt(i)
                when (childView) {
                    is AbilitySelectableEditText -> containerSelectable.removeView(childView)
                }
            }
            tvAddAnotherOccupation.visibility = View.VISIBLE
        }
        abilities.forEach {
            val abilitySelectableEditText = AbilitySelectableEditText(context, it.isMain, this)
            abilitySelectableEditText.setAbility(it)
            containerSelectable.addView(abilitySelectableEditText, containerSelectable.childCount - PRE_LAST_POSITION)
        }
    }

    companion object {
        const val PRE_LAST_POSITION = 1
    }

}