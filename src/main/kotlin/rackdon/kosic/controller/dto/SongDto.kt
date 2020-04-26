package rackdon.kosic.controller.dto

import rackdon.kosic.model.SongCreation
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class SongCreationDto(
        @field:NotBlank(message = ValidationMessage.NOT_BLANCK)
        val name: String?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val albumId: UUID?,
        @field:Positive(message = ValidationMessage.POSITIVE)
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val duration: Int?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val createdOn: LocalDateTime?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val meta: Map<String, Any>?
) {
    companion object {
        fun toModelCreation(songCreationDto: SongCreationDto) =
            SongCreation(name = songCreationDto.name!!,
                    albumId = songCreationDto.albumId!!,
                    duration = songCreationDto.duration!!.toUInt(),
                    createdOn = songCreationDto.createdOn!!,
                    meta = songCreationDto.meta!!
            )
    }
}

data class SongQueryParams(
        override val page: Int?,
        override val pageSize: Int?,
        override val sort: List<String>?,
        override val sortDir: String?,
        val albumId: UUID?,
        val albumName: String?,
        val groupId: UUID?,
        val groupName: String?
) : BaseQueryParams(page, pageSize, sort, sortDir)
