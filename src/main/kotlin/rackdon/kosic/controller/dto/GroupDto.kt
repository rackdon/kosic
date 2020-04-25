package rackdon.kosic.controller.dto

import rackdon.kosic.model.GroupCreation
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PositiveOrZero

data class GroupCreationDto(
        @field:NotBlank(message = ValidationMessage.NOT_BLANCK)
        val name: String?,
        @field:PositiveOrZero(message = ValidationMessage.POSITIVE_OR_ZERO)
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val members: Int?,
        @field:NotNull(message = ValidationMessage.NOT_NULL)
        val createdOn: LocalDateTime?,
        val dissolvedOn: LocalDateTime?
) {
    companion object {
        fun toModelCreation(groupCreationDto: GroupCreationDto) =
            GroupCreation(name = groupCreationDto.name!!,
                    members = groupCreationDto.members!!.toUInt(),
                    createdOn = groupCreationDto.createdOn!!,
                    dissolvedOn = groupCreationDto.dissolvedOn)
    }
}

data class GroupQueryParams(
        override val page: Int?,
        override val pageSize: Int?,
        override val sort: List<String>?,
        override val sortDir: String?
) : BaseQueryParams(page, pageSize, sort, sortDir)
