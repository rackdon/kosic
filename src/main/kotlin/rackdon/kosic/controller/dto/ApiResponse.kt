package rackdon.kosic.controller.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped

sealed class ApiResponse<out T>

data class ResponseSuccess<T>(
        @JsonUnwrapped
        val data: T) : ApiResponse<T>()

data class ResponseError<T>(
        val errors: List<String?>) : ApiResponse<T>()
