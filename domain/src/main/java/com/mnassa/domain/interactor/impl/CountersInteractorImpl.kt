package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.CountersInteractor
import com.mnassa.domain.repository.CountersRepository

/**
 * Created by Peter on 3/7/2018.
 */
class CountersInteractorImpl(private val countersRepository: CountersRepository) : CountersInteractor, CountersRepository by countersRepository {
}