package rackdon.kosic.controller

import arrow.core.None
import arrow.core.Some
import arrow.fx.IO
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import rackdon.kosic.controller.dto.AlbumCreationDto
import rackdon.kosic.controller.dto.AlbumQueryParams
import rackdon.kosic.controller.dto.ResponseError
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.AlbumServiceIOJpa
import rackdon.kosic.utils.generator.albumCreationDto
import rackdon.kosic.utils.generator.albumRaw
import java.net.InetSocketAddress
import java.net.URI
import java.util.UUID

class AlbumControllerIOJpaTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest
    private val albumService = mockk<AlbumServiceIOJpa>()
    private val controllerExceptionHandler = mockk<ControllerExceptionHandler>()
    private val albumController = AlbumControllerIOJpa(albumService, controllerExceptionHandler)

    init {
        // Create Album

        "Create album returns 201 status when all data is correct" {
            val albumCreationDto = Arb.albumCreationDto().single()
            val albumCreation = AlbumCreationDto.toModelCreation(albumCreationDto)
            val albumRaw = Arb.albumRaw().single()
            val host = "localhost:8080"
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            headers.host = InetSocketAddress.createUnresolved("localhost", 8080)

            every { albumService.createAlbum(any()) } returns IO { albumRaw }

            albumController.createAlbum(headers, albumCreationDto) shouldBe
                    ResponseEntity.created(URI("$host/api/albums/${albumRaw.id}")).build()

            verify(exactly = 1) { albumService.createAlbum(albumCreation) }
        }

        "Create album returns 400 status when album group does not exist" {
            val albumCreationDto = Arb.albumCreationDto().single()
            val albumCreation = AlbumCreationDto.toModelCreation(albumCreationDto)
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            headers.host = InetSocketAddress.createUnresolved("localhost", 8080)

            every { albumService.createAlbum(any()) } returns IO { throw GroupNotFound }

            albumController.createAlbum(headers, albumCreationDto) shouldBe
                    ResponseEntity.badRequest().body(ResponseError<Album>(listOf(GroupNotFound.message)))

            verify(exactly = 1) { albumService.createAlbum(albumCreation) }
        }

        "Create album call handleUnexpectedException function" {
            val albumCreationDto = Arb.albumCreationDto().single()
            val albumCreation = AlbumCreationDto.toModelCreation(albumCreationDto)
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            val exception = Exception("My exception")

            every { albumService.createAlbum(any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Album>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            albumController.createAlbum(headers, albumCreationDto) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { albumService.createAlbum(albumCreation) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Album>(exception) }
        }

        // Get albums

        "Get albums without query params" {
            val albumsWithPages = DataWithPages<Album>(emptyList(), 0u)
            val queryParams = AlbumQueryParams(null, null, null, null, null, null)
            every { albumService.getAlbums(any(), any(), any(), any(), any()) } returns
                    IO { albumsWithPages }

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumsWithPages))

            verify(exactly = 1) { albumService.getAlbums(AlbumRaw::class, None, None, None, None) }
        }

        "Get albums with all pagination query params" {
            val album = Arb.albumRaw().single()
            val albumsWithPages = DataWithPages<Album>(listOf(album), 0u)
            val queryParams = AlbumQueryParams(0, 1, listOf("a", "b"), "DESC", null, null)

            every { albumService.getAlbums(any(), any(), any(), any(), any()) } returns IO { albumsWithPages }

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumsWithPages))

            verify(exactly = 1) { albumService.getAlbums(AlbumRaw::class, Some(Page(0u)),
                    Some(PageSize(1u)), Some(listOf("a", "b")), Some(SortDir.DESC)
            ) }
        }

        "Get albums with partial pagination query params" {
            val album = Arb.albumRaw().single()
            val albumsWithPages = DataWithPages<Album>(listOf(album), 0u)
            val queryParams = AlbumQueryParams(0, null, null, "DESC", null, null)

            every { albumService.getAlbums(any(), any(), any(), any(), any()) } returns IO { albumsWithPages }

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumsWithPages))

            verify(exactly = 1) { albumService.getAlbums(AlbumRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
        }

        "Get albums with partial pagination query params and groupId" {
            val groupId = UUID.randomUUID()
            val album = Arb.albumRaw().single()
            val albumsWithPages = DataWithPages<Album>(listOf(album), 0u)
            val queryParams = AlbumQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    groupId = groupId,
                    groupName = null)

            every { albumService.getAlbumsByGroupId(any(), any(), any(), any(), any(), any()) } returns IO { albumsWithPages }

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumsWithPages))

            verify(exactly = 1) { albumService.getAlbumsByGroupId(groupId, AlbumRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get albums with partial pagination query params and groupName" {
            val groupName = "name"
            val album = Arb.albumRaw().single()
            val albumsWithPages = DataWithPages<Album>(listOf(album), 0u)
            val queryParams = AlbumQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    groupId = null,
                    groupName = groupName)

            every { albumService.getAlbumsByGroupName(any(), any(), any(), any(), any(), any()) } returns IO { albumsWithPages }

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumsWithPages))

            verify(exactly = 1) { albumService.getAlbumsByGroupName(groupName, AlbumRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get albums call handleUnexpectedException function" {
            val queryParams = AlbumQueryParams(0, null, null, "DESC", null, null)
            val exception = Exception("My exception")

            every { albumService.getAlbums(any(), any(), any(), any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Album>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            albumController.getAlbums(queryParams) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { albumService.getAlbums(AlbumRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Album>(exception) }
        }

        // Get album by id

        "Get album by id returns 200 status with the album album" {
            val albumRaw = Arb.albumRaw().single()

            every { albumService.getAlbumById(any(), any()) } returns IO { Some(albumRaw) }

            albumController.getAlbumById(albumRaw.id) shouldBe
                    ResponseEntity.ok(ResponseSuccess(albumRaw))

            verify(exactly = 1) { albumService.getAlbumById(albumRaw.id, AlbumRaw::class) }
        }

        "Get album by id returns 404 status if the album album doesn't exist" {
            val albumId = UUID.randomUUID()

            every { albumService.getAlbumById(any(), any()) } returns IO { None }

            albumController.getAlbumById(albumId) shouldBe
                    ResponseEntity.notFound().build()

            verify(exactly = 1) { albumService.getAlbumById(albumId, AlbumRaw::class) }
        }

        "Get album by id call handleUnexpectedException function" {
            val albumId = UUID.randomUUID()
            val exception = Exception("My exception")

            every { albumService.getAlbumById(any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Album>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            albumController.getAlbumById(albumId) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { albumService.getAlbumById(albumId, AlbumRaw::class) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Album>(exception) }
        }
    }
}
