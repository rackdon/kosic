package rackdon.kosic.utils

import io.kotlintest.properties.Gen
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import java.time.LocalDateTime
import java.util.UUID

class Generator {

    private fun nonEmptyString(@Suppress("SameParameterValue") stringLimit: Int?) =
        Gen.string(stringLimit ?: 100).filterNot { it.isEmpty() }

    fun generateGroupCreation(
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            members: UInt = Gen.positiveIntegers().random().first().toUInt(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first(),
            dissolvedOn: LocalDateTime? = Gen.localDateTime().orNull().random().first()) =
        GroupCreation(name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateGroupRaw(
            id: UUID = Gen.uuid().random().first(),
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            members: UInt = Gen.positiveIntegers().random().first().toUInt(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first(),
            dissolvedOn: LocalDateTime? = Gen.localDateTime().orNull().random().first()
    ) =
        GroupRaw(id = id, name = name, members = members, createdOn = createdOn, dissolvedOn = dissolvedOn)

    fun generateAlbumCreation(
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            groupId: UUID = Gen.uuid().random().first(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first()
    ) =
        AlbumCreation(name = name, groupId = groupId, createdOn = createdOn)

    fun generateAlbumRaw(
            id: UUID = Gen.uuid().random().first(),
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            groupId: UUID = Gen.uuid().random().first(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first()
    ) =
        AlbumRaw(id = id, name = name, groupId = groupId, createdOn = createdOn)

    fun generateSongCreation(
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            albumId: UUID = Gen.uuid().random().first(),
            duration: UInt = Gen.positiveIntegers().random().first().toUInt(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first(),
            meta: Map<String, Any> = Gen.map(Gen.string(STRING_LIMIT), Gen.string(STRING_LIMIT), MAP_LIMIT).random().first()
    ) =
        SongCreation(name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    fun generateSongRaw(
            id: UUID = Gen.uuid().random().first(),
            name: String = nonEmptyString(STRING_LIMIT).random().first(),
            albumId: UUID = Gen.uuid().random().first(),
            duration: UInt = Gen.positiveIntegers().random().first().toUInt(),
            createdOn: LocalDateTime = Gen.localDateTime().random().first(),
            meta: Map<String, Any> = Gen.map(Gen.string(STRING_LIMIT), Gen.string(STRING_LIMIT), MAP_LIMIT).random().first()
    ) =
        SongRaw(id = id, name = name, albumId = albumId, duration = duration, createdOn = createdOn, meta = meta)

    companion object {
        private const val STRING_LIMIT = 7
        private const val MAP_LIMIT = 7
    }
}
