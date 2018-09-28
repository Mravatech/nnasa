package com.mnassa.screen.photopager

import com.mnassa.screen.base.MnassaViewModel

/**
 * Created by Peter on 9/11/2018.
 */
interface PhotoPagerViewModel : MnassaViewModel {
    fun loadImage(imageUrl: String)
}