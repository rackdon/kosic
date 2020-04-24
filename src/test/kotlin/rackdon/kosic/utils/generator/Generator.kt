package rackdon.kosic.utils.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.single

object GeneratorConstants {

    const val STRING_LIMIT = 7
    const val MAP_LIMIT = 7
}

fun <T> T.orNull(): T? {
    return if (Arb.bool().single()) this else null
}
