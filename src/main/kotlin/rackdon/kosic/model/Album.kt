package rackdon.kosic.model

import java.time.LocalDateTime
import java.util.UUID

sealed class Album {
    abstract val name: String
    abstract val groupId: UUID
    abstract val createdOn: LocalDateTime
}

data class AlbumRaw(
        val id: UUID,
        override val name: String,
        override val groupId: UUID,
        override val createdOn: LocalDateTime
) : Album()

data class AlbumCreation(
        override val name: String,
        override val groupId: UUID,
        override val createdOn: LocalDateTime
) : Album()
