package com.mnassa.screen.base

/**
 * @author Artem Chepurnoy
 */
sealed class ProgressEvent

class ShowProgressEvent(
    val hideKeyboard: Boolean = HIDE_KEYBOARD
) : ProgressEvent() {
    companion object {
        const val HIDE_KEYBOARD = true
    }
}

class HideProgressEvent : ProgressEvent()
