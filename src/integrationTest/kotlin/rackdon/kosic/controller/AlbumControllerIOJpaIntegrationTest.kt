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
import rackdon.kosic.service.AlbumServiceIOJpa

@Suppress("BlockingMethodInNonBlockingContext")
@WebFluxTest(AlbumControllerIOJpa::class)
class AlbumControllerIOJpaIntegrationTest(webClient: WebTestClient) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    @MockkBean
    lateinit var albumService: AlbumServiceIOJpa
    private val objectMapper = jacksonObjectMapper()

    init {
        // Create Album

        "Create album returns 400 status with a list of error validations" {
            val body = emptyMap<String, Any>()

            webClient.post()
                .uri("/api/albums")
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().json(objectMapper.writeValueAsString(
                        BadRequest(listOf("name cannot be empty", "groupId cannot be null", "createdOn cannot be null"))))

            verify(exactly = 0) { albumService.createAlbum(any()) }
        }

        // Get albums

        "Get albums query params are invalid and unknown are ignored" {
            webClient.get()
                .uri(UriComponentsBuilder
                    .fromPath("/api/albums")
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

            verify(exactly = 0) { albumService.getAlbums(any(), any(), any(), any(), any()) }
        }
    }
}
