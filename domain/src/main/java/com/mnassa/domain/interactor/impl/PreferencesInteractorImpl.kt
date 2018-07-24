package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.PreferencesInteractor
import com.mnassa.domain.repository.PreferencesRepository

/**
 * Created by Peter on 7/24/2018.
 */
class PreferencesInteractorImpl(private val repository: PreferencesRepository) : PreferencesInteractor by repository