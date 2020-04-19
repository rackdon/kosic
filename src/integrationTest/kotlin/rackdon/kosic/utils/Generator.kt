package rackdon.kosic.utils

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.take
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

    inline fun <reified T>T.orNull(): T? {
       return if (Arb.bool().take(1).first()) this else null
    }

    fun generateGroupCreation(
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            members: UInt = Arb.positiveInts(32767).take(1).first().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first(),
            dissolvedOn: LocalDateTime? = Arb.localDateTime().take(1).first().orNull()) =
        GroupCreation(name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateGroupRaw(
            id: UUID = Arb.uuid().take(1).first(),
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            members: UInt = Arb.positiveInts().take(1).first().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first(),
            dissolvedOn: LocalDateTime? = Arb.localDateTime().take(1).first().orNull()
    ) =
        GroupRaw(id = id, name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateAlbumCreation(
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            groupId: UUID = Arb.uuid().take(1).first(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first()
    ) =
        AlbumCreation(name = name, groupId = groupId, createdOn = createdOn)

    fun generateAlbumRaw(
            id: UUID = Arb.uuid().take(1).first(),
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            groupId: UUID = Arb.uuid().take(1).first(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first()
    ) =
        AlbumRaw(id = id, name = name, groupId = groupId, createdOn = createdOn)

    fun generateSongCreation(
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            albumId: UUID = Arb.uuid().take(1).first(),
            duration: UInt = Arb.positiveInts().take(1).first().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first(),
            meta: Map<String, Any> = Arb.map(Arb.string(STRING_LIMIT), Arb.string(STRING_LIMIT), MAP_LIMIT).take(1).first()
    ) =
        SongCreation(name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    fun generateSongRaw(
            id: UUID = Arb.uuid().take(1).first(),
            name: String = Arb.string(1, STRING_LIMIT).take(1).first(),
            albumId: UUID = Arb.uuid().take(1).first(),
            duration: UInt = Arb.positiveInts().take(1).first().toUInt(),
            createdOn: LocalDateTime = Arb.localDateTime().take(1).first(),
            meta: Map<String, Any> = Arb.map(Arb.string(STRING_LIMIT), Arb.string(STRING_LIMIT), MAP_LIMIT).take(1).first()
    ) =
        SongRaw(id = id, name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    companion object {
        private const val STRING_LIMIT = 7
        private const val MAP_LIMIT = 7
    }
}
