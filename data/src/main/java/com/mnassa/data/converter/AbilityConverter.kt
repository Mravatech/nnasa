package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.retrofit.request.Ability
import com.mnassa.domain.model.AccountAbility

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/19/2018
 */

class AbilityConverter: ConvertersContextRegistrationCallback  {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAbility)
    }
    private fun convertAbility(input: AccountAbility): Ability {
        return Ability(input.name, input.place, input.isMain)
    }
}