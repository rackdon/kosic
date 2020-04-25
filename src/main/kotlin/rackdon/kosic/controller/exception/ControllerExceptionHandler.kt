package rackdon.kosic.controller.exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import rackdon.kosic.controller.dto.ResponseError
import reactor.core.publisher.Mono

@Component
@Order(-2)
class ControllerExceptionHandler(applicationContext: ApplicationContext, configurer: ServerCodecConfigurer) :
    AbstractErrorWebExceptionHandler(DefaultErrorAttributes(), ResourceProperties(), applicationContext) {

    init {
        super.setMessageWriters(configurer.writers)
        super.setMessageReaders(configurer.readers)
    }

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    private fun decodingExceptionMessage(error: DecodingException): List<String> {
        return when (error.cause) {
            is InvalidFormatException -> {
                val castedError = (error.cause as InvalidFormatException)
                listOf("${castedError.value} must be of type ${castedError.targetType.name}")
            }
            is MissingKotlinParameterException -> listOf("${(error.cause as MissingKotlinParameterException).parameter.name} cannot be null")
            is MismatchedInputException -> {
                val castedError = (error.cause as MismatchedInputException)
                listOf("${castedError.path[0].fieldName} must be of type ${castedError.targetType.name}")
            }
            else -> {
                log.error("Cannot manage decoding error cause ${error.cause?.javaClass}. Necessary to implement it", error)
                listOf(error.message ?: "")
            }
        }
    }

    private fun serverWebInputExceptionMessage(error: ServerWebInputException): List<String> {
        return when {
            error.cause is DecodingException -> decodingExceptionMessage((error.cause as DecodingException))
            error.cause is TypeMismatchException -> {
                val castedError = (error.cause as TypeMismatchException)
                listOf("${castedError.value} must be of type ${castedError.requiredType?.name}")
            }
            error.reason != null && error.reason!!.contains("Request body is missing") ->
                listOf("Request body is missing")
            else -> {
                log.error("Cannot manage server web input error type ${error.cause?.javaClass} " +
                        "or server web input error cause ${error.cause?.cause?.javaClass}. Necessary to implement it", error)
                listOf(error.message)
            }
        }
    }

    /**
     * Form a list of message errors based on the current error.
     * @param error - the request error
     * @return a list of messages based on the error
     */
    private fun errorMsg(error: Throwable): List<String> {
        return when (error) {
            is WebExchangeBindException ->
                error.bindingResult.allErrors.map { "${(it as FieldError).field} ${it.defaultMessage}" }
            is ServerWebInputException -> serverWebInputExceptionMessage(error)
            else -> {
                log.error("Cannot manage error type ${error::class}. Necessary to implement it", error)
                listOf(error.message ?: "")
            }
        }
    }

    /**
     * Form the response body based on the request status and the error type
     * @param status - the request status
     * @param error - the request error
     * @return the controller exception response with the proper body
     */
    private fun errorBody(status: HttpStatus, error: Throwable): ControllerExceptionResponse {
        return when (status.value()) {
            400 -> {
                val errorMsg = errorMsg(error)
                BadRequest(errorMsg)
            }
            404 -> NotFound
            500 -> {
                log.error("Internal server error", error)
                InternalServerError
            }
            else -> {
                log.error("Unhandled status exception $status", error)
                InternalServerError
            }
        }
    }

    /**
     * Handle the current error request returning a proper response
     * @param request - the current request
     * @return a response for the current error request
     */
    private fun handleRequest(request: ServerRequest): Mono<ServerResponse> {
        val error = (getError(request) as java.lang.Exception)
        val status = if (error is ResponseStatusException) error.status else HttpStatus.INTERNAL_SERVER_ERROR
        val body = errorBody(status, error)
        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes?): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), HandlerFunction(this::handleRequest))
    }

    private fun <T> handlePsqlException(exception: PSQLException): ResponseEntity<ResponseError<T>> {
        return when (exception.sqlState) {
            PSQLState.UNIQUE_VIOLATION.state -> ResponseEntity.status(HttpStatus.CONFLICT).build()
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    fun <T> handleUnexpectedException(error: Throwable): ResponseEntity<ResponseError<T>> {
        return when {
            error.cause?.cause is PSQLException -> handlePsqlException((error.cause?.cause as PSQLException))
            else -> {
                log.error("Internal server error: ${error.message}", error)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }
}
