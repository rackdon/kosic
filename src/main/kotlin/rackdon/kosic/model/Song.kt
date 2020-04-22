package rackdon.kosic.model

import java.time.LocalDateTime
import java.util.UUID

sealed class Song {
    abstract val name: String
    abstract val duration: UInt
    abstract val createdOn: LocalDateTime
    abstract val meta: Map<String, Any>
}

data class SongBase(
        override val name: String,
        override val duration: UInt,
        override val createdOn: LocalDateTime,
        override val meta: Map<String, Any>
) : Song()

data class SongRaw(
        val id: UUID,
        override val name: String,
        val albumId: UUID,
        override val duration: UInt,
        override val createdOn: LocalDateTime,
        override val meta: Map<String, Any>
) : Song()

data class SongCreation(
        override val name: String,
        val albumId: UUID,
        override val duration: UInt,
        override val createdOn: LocalDateTime,
        override val meta: Map<String, Any>
) : Song()

data class SongWithAlbum(
        val id: UUID,
        override val name: String,
        val album: Album,
        override val duration: UInt,
        override val createdOn: LocalDateTime,
        override val meta: Map<String, Any>
) : Song()
