package com.mnassa.core.converter

import kotlin.reflect.KFunction3


/**
 * Context of converters interface.
 */
interface ConvertersContext {

    /**
     * Registers converter between two entity classes
     *
     * @param inClass   input class - abstract as possible
     * @param outClass  output class - concrete as possible
     * @param converter convert function reference
     *
     * Sample:
     * convertersContext.registerConverter(UserRest::class.java, UserDb::class.java, this::restToDb)
     * private fun restToDb(rest: UserRest, token: Any?, convertersContext: ConvertersContext): UserDb {...}
     */
    fun <IN : Any, OUT : Any> registerConverter(
            inClass: Class<IN>,
            outClass: Class<OUT>,
            converter: KFunction3<
                    @ParameterName(name = "input") IN,
                    @ParameterName(name = "token") Any?,
                    @ParameterName(name = "convertersContext") ConvertersContext,
                    OUT>)

    /**
     * Registers converter between two entity classes
     *
     * @param inClass   input class - abstract as possible
     * @param outClass  output class - concrete as possible
     * @param converter convert function reference
     */
    fun <IN : Any, OUT : Any> registerConverter(
            inClass: Class<IN>,
            outClass: Class<OUT>,
            converter: (IN, Any?, ConvertersContext) -> OUT) {
        registerConverter(inClass, outClass, converter::invoke)
    }

    /**
     * Convert single object to another type.
     *
     * @param input     some object
     * @param token     token that will be passed in [ConvertersContext.convert] method.
     * @param outClass  class of type you want convert object to
     * @return converted object
     */
    fun <IN : Any, OUT : Any> convert(input: IN, token: Any?, outClass: Class<OUT>): OUT

    fun <IN : Any, OUT : Any> convert(input: IN, outClass: Class<OUT>): OUT = convert(input, Unit, outClass)

    /**
     * Convert collection of objects to another type.
     *
     * @param collection collection of objects
     * @param token      token that will be passed in [ConvertersContext.convert] method.
     * @param outClass   class of type you want convert collection to
     * @return collection of converted objects
     */
    fun <IN : Any, OUT : Any> convertCollection(collection: Iterable<IN>, token: Any?, outClass: Class<OUT>): List<OUT>

    fun <IN : Any, OUT : Any> convertCollection(collection: Iterable<IN>, outClass: Class<OUT>): List<OUT> = convertCollection(collection, Unit, outClass)

    /**
     * Returns converter as a function
     *
     * @param outClass output class
     * @param <IN>     type of input object
     * @param <OUT>    type of output object
     * @return converted object with type OUT
     */
    fun <IN : Any, OUT : Any> convertFunc(outClass: Class<OUT>): (input: IN) -> OUT = {
        convert(it, outClass)
    }

    fun <IN : Any, OUT : Any> convertFunc(outClass: Class<OUT>, token: Any?): (input: IN) -> OUT = {
        convert(it, token, outClass)
    }

    /**
     * Returns converter as a function
     *
     * @param outClass output class
     * @param <IN>     type of input objects
     * @param <OUT>    type of output objects
     * @return converted list of objects with type OUT
     */
    fun <IN : Any, OUT : Any> convertCollectionFunc(outClass: Class<OUT>): (input: Iterable<IN>) -> List<OUT> = {
        convertCollection(it, outClass)
    }
}