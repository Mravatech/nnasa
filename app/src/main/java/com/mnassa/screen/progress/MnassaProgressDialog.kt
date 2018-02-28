package com.mnassa.screen.progress

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.annotation.StyleRes
import com.mnassa.R

class MnassaProgressDialog : ProgressDialog {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, @StyleRes themeResId: Int) : super(context, themeResId) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress)
    }
}
