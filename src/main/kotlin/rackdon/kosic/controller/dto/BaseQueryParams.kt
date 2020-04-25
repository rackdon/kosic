package rackdon.kosic.controller.dto

import javax.validation.constraints.Pattern
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

abstract class BaseQueryParams(
        @field:PositiveOrZero(message = ValidationMessage.POSITIVE_OR_ZERO)
        open val page: Int?,
        @field:Positive(message = ValidationMessage.POSITIVE)
        open val pageSize: Int?,
        open val sort: List<String>?,
        @field:Pattern(regexp = "ASC|DESC", message = "must be ASC or DESC")
        open val sortDir: String?
)
