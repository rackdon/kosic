package rackdon.kosic.utils.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import java.time.LocalDateTime
import java.util.UUID

fun Arb.Companion.albumCreation(
        name: String? = null,
        groupId: UUID? = null,
        createdOn: LocalDateTime? = null
) =
    arb { generateSequence {
        AlbumCreation(
                name = name ?: Arb.string(1, GeneratorConstants.STRING_LIMIT).single(),
                groupId = groupId ?: Arb.uuid().single(),
                createdOn = createdOn ?: Arb.localDateTime().single())
    } }

fun Arb.Companion.albumRaw(
        id: UUID? = null,
        name: String? = null,
        groupId: UUID? = null,
        createdOn: LocalDateTime? = null
) =
    arb { generateSequence {
        AlbumRaw(
                id = id ?: Arb.uuid().single(),
                name = name ?: Arb.string(1, GeneratorConstants.STRING_LIMIT).single(),
                groupId = groupId ?: Arb.uuid().single(),
                createdOn = createdOn ?: Arb.localDateTime().single())
    } }
