package com.mnassa.screen.main

import com.bluelinelabs.conductor.Controller

/**
 * Created by Peter on 10.03.2018.
 */
interface OnPageSelected {
    fun onPageSelected()
    fun onPageUnSelected() {}
}

interface PageContainer {
    fun isPageSelected(page: Controller): Boolean
}

interface OnScrollToTop {
    fun scrollToTop()
}