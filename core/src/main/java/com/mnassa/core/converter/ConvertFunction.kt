package com.mnassa.core.converter

/**
 * Created by Peter on 8/14/2018.
 */
@FunctionalInterface
interface ConvertFunction1<IN : Any?, OUT : Any?> {
    suspend fun convert(input: IN): OUT
}

@FunctionalInterface
interface ConvertFunction2<IN : Any?, OUT : Any?> {
    suspend fun convert(input: IN, tag: Any?): OUT
}

@FunctionalInterface
interface ConvertFunction3<IN : Any?, OUT : Any?> {
    suspend fun convert(input: IN, tag: Any?, context: ConvertersContext): OUT
}