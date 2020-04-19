package rackdon.kosic.model

import java.time.LocalDateTime
import java.util.UUID

sealed class Group {
    abstract val name: String
    abstract val members: UInt
    abstract val createdOn: LocalDateTime
    abstract val dissolvedOn: LocalDateTime?
}

data class GroupRaw(
        val id: UUID,
        override val name: String,
        override val members: UInt,
        override val createdOn: LocalDateTime,
        override val dissolvedOn: LocalDateTime?
) : Group()

data class GroupCreation(
        override val name: String,
        override val members: UInt,
        override val createdOn: LocalDateTime,
        override val dissolvedOn: LocalDateTime?
) : Group()
