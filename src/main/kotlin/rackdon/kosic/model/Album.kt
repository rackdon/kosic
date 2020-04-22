package rackdon.kosic.model

import java.time.LocalDateTime
import java.util.UUID

sealed class Album {
    abstract val name: String
    abstract val createdOn: LocalDateTime
}

data class AlbumBase(
    override val name: String,
    override val createdOn: LocalDateTime
) : Album()

data class AlbumRaw(
        val id: UUID,
        override val name: String,
        val groupId: UUID,
        override val createdOn: LocalDateTime
) : Album()

data class AlbumCreation(
        override val name: String,
        val groupId: UUID,
        override val createdOn: LocalDateTime
) : Album()

data class AlbumWithGroup(
        val id: UUID,
        override val name: String,
        val group: Group,
        override val createdOn: LocalDateTime
) : Album()
