package rackdon.kosic.controller.exception

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.hibernate.exception.ConstraintViolationException
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import org.springframework.context.ApplicationContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerCodecConfigurer

class ControllerExceptionHandlerTest : StringSpec() {
    private val applicationContextMock = mockk<ApplicationContext> {}
    private lateinit var controllerExceptionHandler: ControllerExceptionHandler

    override fun beforeSpec(spec: Spec) {
        every { applicationContextMock.classLoader } returns ClassLoader.getSystemClassLoader()
        controllerExceptionHandler = ControllerExceptionHandler(applicationContextMock, ServerCodecConfigurer.create())
    }

    init {
        "Manage psql unique violation error returns 409 status response" {
            val exception =
                DataIntegrityViolationException("could not execute statement",
                        ConstraintViolationException("could not execute statement",
                                PSQLException("Duplicate key value", PSQLState.UNIQUE_VIOLATION), "plan_groups_name_key")
                )
            controllerExceptionHandler.handleUnexpectedException<String>(exception) shouldBe
                    ResponseEntity.status(HttpStatus.CONFLICT).build<String>()
        }

        "Unhandled psql state returns 500 status response" {
            val exception =
                DataIntegrityViolationException("could not execute statement",
                        ConstraintViolationException("could not execute statement",
                                PSQLException("Unexpected exception", PSQLState.UNKNOWN_STATE), "plan_groups_name_key")
                )
            controllerExceptionHandler.handleUnexpectedException<String>(exception) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<String>()
        }

        "Unhandled exception cause returns 500 status response" {
            val exception = Exception("Unhandled")
            controllerExceptionHandler.handleUnexpectedException<String>(exception) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<String>()
        }
    }
}
