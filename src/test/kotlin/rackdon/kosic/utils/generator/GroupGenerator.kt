package rackdon.kosic.utils.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import rackdon.kosic.controller.dto.GroupCreationDto
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.utils.generator.GeneratorConstants.STRING_LIMIT
import java.time.LocalDateTime
import java.util.UUID

fun Arb.Companion.groupCreation(
        name: String? = null,
        members: UInt? = null,
        createdOn: LocalDateTime? = null,
            dissolvedOn: LocalDateTime? = null
) =
    arb { generateSequence {
        GroupCreation(
                name = name ?: Arb.string(1, STRING_LIMIT).single(),
                members = members ?: Arb.positiveInts(32767).single().toUInt(),
                createdOn = createdOn ?: Arb.localDateTime().single(),
                dissolvedOn = dissolvedOn ?: Arb.localDateTime().single().orNull())
    } }

fun Arb.Companion.groupCreationDto(
        name: String? = null,
        members: Int? = null,
        createdOn: LocalDateTime? = null,
        dissolvedOn: LocalDateTime? = null
) =
    arb { generateSequence {
        GroupCreationDto(
                name = name ?: Arb.string(1, STRING_LIMIT).single(),
                members = members ?: Arb.positiveInts(32767).single(),
                createdOn = createdOn ?: Arb.localDateTime().single(),
                dissolvedOn = dissolvedOn ?: Arb.localDateTime().single().orNull())
    } }

fun Arb.Companion.groupRaw(
        id: UUID? = null,
        name: String? = null,
        members: UInt? = null,
        createdOn: LocalDateTime? = null,
        dissolvedOn: LocalDateTime? = null
) =
    arb { generateSequence {
        GroupRaw(
                id = id ?: Arb.uuid().single(),
                name = name ?: Arb.string(1, STRING_LIMIT).single(),
                members = members ?: Arb.positiveInts(32767).single().toUInt(),
                createdOn = createdOn ?: Arb.localDateTime().single(),
                dissolvedOn = dissolvedOn ?: Arb.localDateTime().single().orNull())
    } }
