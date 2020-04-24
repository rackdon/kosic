package rackdon.kosic.utils.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.utils.generator.GeneratorConstants.MAP_LIMIT
import rackdon.kosic.utils.generator.GeneratorConstants.STRING_LIMIT
import java.time.LocalDateTime
import java.util.UUID

fun Arb.Companion.songCreation(
        name: String? = null,
        albumId: UUID? = null,
        duration: UInt? = null,
        createdOn: LocalDateTime? = null,
        meta: Map<String, Any>? = null
) =
    arb { generateSequence {
        SongCreation(
                name = name ?: Arb.string(1, GeneratorConstants.STRING_LIMIT).single(),
                albumId = albumId ?: Arb.uuid().single(),
                duration = duration ?: Arb.positiveInts().single().toUInt(),
                createdOn = createdOn ?: Arb.localDateTime().single(),
                meta = meta ?: Arb.map(Arb.string(1, STRING_LIMIT), Arb.string(1, STRING_LIMIT), 0, MAP_LIMIT).single())
    } }

fun Arb.Companion.songRaw(
        id: UUID? = null,
        name: String? = null,
        albumId: UUID? = null,
        duration: UInt? = null,
        createdOn: LocalDateTime? = null,
        meta: Map<String, Any>? = null
) =
    arb { generateSequence {
        SongRaw(
                id = id ?: Arb.uuid().single(),
                name = name ?: Arb.string(1, STRING_LIMIT).single(),
                albumId = albumId ?: Arb.uuid().single(),
                duration = duration ?: Arb.positiveInts().single().toUInt(),
                createdOn = createdOn ?: Arb.localDateTime().single(),
                meta = meta ?: Arb.map(Arb.string(1, STRING_LIMIT), Arb.string(1, STRING_LIMIT), 0, MAP_LIMIT).single())
    } }
