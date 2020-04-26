package rackdon.kosic.controller.dto

import rackdon.kosic.model.AlbumCreation
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class AlbumCreationDto(
        @field:NotBlank(message = ValidationMessage.NOT_BLANCK)
        val name: String?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val groupId: UUID?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val createdOn: LocalDateTime?
) {
    companion object {
        fun toModelCreation(albumCreationDto: AlbumCreationDto) =
            AlbumCreation(name = albumCreationDto.name!!,
                    groupId = albumCreationDto.groupId!!,
                    createdOn = albumCreationDto.createdOn!!)
    }
}

data class AlbumQueryParams(
        override val page: Int?,
        override val pageSize: Int?,
        override val sort: List<String>?,
        override val sortDir: String?,
        val groupId: UUID?,
        val groupName: String?
) : BaseQueryParams(page, pageSize, sort, sortDir)
