package com.mnassa.domain.model

/**
 * @author Artem Chepurnoy
 */
enum class Plural(val suffix: String) {
    OTHER("__other"),
    ZERO("__zero"),
    ONE("__one"),
    TWO("__two"),
    FEW("__few"),
    MANY("__many");
}
