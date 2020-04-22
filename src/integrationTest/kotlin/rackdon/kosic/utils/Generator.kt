package rackdon.kosic.utils

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import java.time.LocalDateTime
import java.util.UUID

class Generator {

    private inline fun <reified T> T.orNull(): T? {
        return if (Arb.bool().single()) this else null
    }

    fun generateGroupCreation(
            name: String = Arb.string(1, STRING_LIMIT).single(),
            members: UInt = Arb.positiveInts(32767).single().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().single(),
            dissolvedOn: LocalDateTime? = Arb.localDateTime().single().orNull()) =
        GroupCreation(name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateGroupRaw(
            id: UUID = Arb.uuid().single(),
            name: String = Arb.string(1, STRING_LIMIT).single(),
            members: UInt = Arb.positiveInts().single().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().single(),
            dissolvedOn: LocalDateTime? = Arb.localDateTime().single().orNull()
    ) =
        GroupRaw(id = id, name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateAlbumCreation(
            name: String = Arb.string(1, STRING_LIMIT).single(),
            groupId: UUID = Arb.uuid().single(),
            createdOn: LocalDateTime = Arb.localDateTime().single()
    ) =
        AlbumCreation(name = name, groupId = groupId, createdOn = createdOn)

    fun generateAlbumRaw(
            id: UUID = Arb.uuid().single(),
            name: String = Arb.string(1, STRING_LIMIT).single(),
            groupId: UUID = Arb.uuid().single(),
            createdOn: LocalDateTime = Arb.localDateTime().single()
    ) =
        AlbumRaw(id = id, name = name, groupId = groupId, createdOn = createdOn)

    fun generateSongCreation(
            name: String = Arb.string(1, STRING_LIMIT).single(),
            albumId: UUID = Arb.uuid().single(),
            duration: UInt = Arb.positiveInts().single().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().single(),
            meta: Map<String, Any> = Arb.map(Arb.string(STRING_LIMIT), Arb.string(STRING_LIMIT), MAP_LIMIT).single()
    ) =
        SongCreation(name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    fun generateSongRaw(
            id: UUID = Arb.uuid().single(),
            name: String = Arb.string(1, STRING_LIMIT).single(),
            albumId: UUID = Arb.uuid().single(),
            duration: UInt = Arb.positiveInts().single().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().single(),
            meta: Map<String, Any> = Arb.map(Arb.string(STRING_LIMIT), Arb.string(STRING_LIMIT), MAP_LIMIT).single()
    ) =
        SongRaw(id = id, name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    companion object {
        private const val STRING_LIMIT = 7
        private const val MAP_LIMIT = 7
    }
}
