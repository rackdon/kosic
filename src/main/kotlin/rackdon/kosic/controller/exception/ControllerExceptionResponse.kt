package rackdon.kosic.controller.exception

sealed class ControllerExceptionResponse

data class BadRequest(val errors: List<String>) : ControllerExceptionResponse()
object NotFound : ControllerExceptionResponse()
object InternalServerError : ControllerExceptionResponse()
