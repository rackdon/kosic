package rackdon.kosic.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.spring.SpringListener
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import rackdon.kosic.controller.exception.BadRequest
import rackdon.kosic.service.SongServiceIOJpa

@Suppress("BlockingMethodInNonBlockingContext")
@WebFluxTest(SongControllerIOJpa::class)
class SongControllerIOJpaIntegrationTest(webClient: WebTestClient) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    @MockkBean
    lateinit var songService: SongServiceIOJpa
    private val objectMapper = jacksonObjectMapper()

    init {
        // Create Song

        "Create song returns 400 status with a list of error validations" {
            val body = emptyMap<String, Any>()

            webClient.post()
                .uri("/api/songs")
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().json(objectMapper.writeValueAsString(
                        BadRequest(listOf("name cannot be empty", "albumId cannot be null", "duration cannot be null",
                                "createdOn cannot be null", "meta cannot be null"))))

            verify(exactly = 0) { songService.createSong(any()) }
        }

        // Get songs

        "Get songs query params are invalid and unknown are ignored" {
            webClient.get()
                .uri(UriComponentsBuilder
                    .fromPath("/api/songs")
                    .queryParam("page", -1)
                    .queryParam("pageSize", -1)
                    .queryParam("sortDir", "FOO")
                    .queryParam("foo", "faa")
                    .toUriString())
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().json(objectMapper.writeValueAsString(
                        BadRequest(listOf("page must be greater than or equal to 0",
                                "pageSize must be greater than 0", "sortDir must be ASC or DESC"))))

            verify(exactly = 0) { songService.getSongs(any(), any(), any(), any(), any()) }
        }
    }
}
