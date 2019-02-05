package com.mnassa.screen.group.profile

import com.google.android.material.appbar.AppBarLayout

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    var currentState = State.IDLE
        private set
    var currentOffset = 0f
        private set

    enum class State {
        EXPANDED, COLLAPSED, IDLE
    }

    fun reset() {
        currentState = State.IDLE
        currentOffset = 0f
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        when {
            i == 0 -> {
                if (currentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED)
                }
                currentState = State.EXPANDED
            }
            Math.abs(i) >= appBarLayout.totalScrollRange -> {
                if (currentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED)
                }
                currentState = State.COLLAPSED
            }
            else -> {
                if (currentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE)
                }
                currentState = State.IDLE
            }
        }
        val offset = Math.abs(i / appBarLayout.totalScrollRange.toFloat())
        if (offset != currentOffset) {
            currentOffset = offset
            onOffsetChanged(currentState, offset)
        }
    }

    open fun onStateChanged(appBarLayout: AppBarLayout, state: State) {}

    open fun onOffsetChanged(state: State, offset: Float) { }
}