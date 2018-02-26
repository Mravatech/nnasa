package com.mnassa.core.addons

/**
 * Created by Peter on 2/20/2018.
 */
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Peter on 18.02.2018.
 */
private val requestCodeGenerator = AtomicInteger(90)

fun generateRequestCode() = requestCodeGenerator.getAndIncrement()