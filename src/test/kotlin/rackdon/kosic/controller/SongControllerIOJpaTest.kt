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
import rackdon.kosic.controller.dto.SongCreationDto
import rackdon.kosic.controller.dto.SongQueryParams
import rackdon.kosic.controller.dto.ResponseError
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.SongServiceIOJpa
import rackdon.kosic.utils.generator.songCreationDto
import rackdon.kosic.utils.generator.songRaw
import java.net.InetSocketAddress
import java.net.URI
import java.util.UUID

class SongControllerIOJpaTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest
    private val songService = mockk<SongServiceIOJpa>()
    private val controllerExceptionHandler = mockk<ControllerExceptionHandler>()
    private val songController = SongControllerIOJpa(songService, controllerExceptionHandler)

    init {
        // Create Song

        "Create song returns 201 status when all data is correct" {
            val songCreationDto = Arb.songCreationDto().single()
            val songCreation = SongCreationDto.toModelCreation(songCreationDto)
            val songRaw = Arb.songRaw().single()
            val host = "localhost:8080"
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            headers.host = InetSocketAddress.createUnresolved("localhost", 8080)

            every { songService.createSong(any()) } returns IO { songRaw }

            songController.createSong(headers, songCreationDto) shouldBe
                    ResponseEntity.created(URI("$host/api/songs/${songRaw.id}")).build()

            verify(exactly = 1) { songService.createSong(songCreation) }
        }

        "Create song returns 400 status when song group does not exist" {
            val songCreationDto = Arb.songCreationDto().single()
            val songCreation = SongCreationDto.toModelCreation(songCreationDto)
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            headers.host = InetSocketAddress.createUnresolved("localhost", 8080)

            every { songService.createSong(any()) } returns IO { throw GroupNotFound }

            songController.createSong(headers, songCreationDto) shouldBe
                    ResponseEntity.badRequest().body(ResponseError<Song>(listOf(GroupNotFound.message)))

            verify(exactly = 1) { songService.createSong(songCreation) }
        }

        "Create song call handleUnexpectedException function" {
            val songCreationDto = Arb.songCreationDto().single()
            val songCreation = SongCreationDto.toModelCreation(songCreationDto)
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            val exception = Exception("My exception")

            every { songService.createSong(any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Song>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            songController.createSong(headers, songCreationDto) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { songService.createSong(songCreation) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Song>(exception) }
        }

        // Get songs

        "Get songs without query params" {
            val songsWithPages = DataWithPages<Song>(emptyList(), 0u)
            val queryParams = SongQueryParams(
                    page = null,
                    pageSize = null,
                    sort = null,
                    sortDir = null,
                    albumId = null,
                    albumName = null,
                    groupId = null,
                    groupName = null
            )
            every { songService.getSongs(any(), any(), any(), any(), any()) } returns
                    IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongs(SongRaw::class, None, None, None, None) }
        }

        "Get songs with all pagination query params" {
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = 1,
                    sort = listOf("a", "b"),
                    sortDir = "DESC",
                    albumId = null,
                    albumName = null,
                    groupId = null,
                    groupName = null
            )

            every { songService.getSongs(any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongs(SongRaw::class, Some(Page(0u)),
                    Some(PageSize(1u)), Some(listOf("a", "b")), Some(SortDir.DESC)
            ) }
        }

        "Get songs with partial pagination query params" {
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = null,
                    albumName = null,
                    groupId = null,
                    groupName = null
            )

            every { songService.getSongs(any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongs(SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
        }
        "Get songs with partial pagination query params and albumId" {
            val albumId = UUID.randomUUID()
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = albumId,
                    albumName = null,
                    groupId = null,
                    groupName = null)

            every { songService.getSongsByAlbumId(any(), any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongsByAlbumId(albumId, SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get songs with partial pagination query params and albumName" {
            val albumName = "name"
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = null,
                    albumName = albumName,
                    groupId = null,
                    groupName = null)

            every { songService.getSongsByAlbumName(any(), any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongsByAlbumName(albumName, SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get songs with partial pagination query params and groupId" {
            val groupId = UUID.randomUUID()
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = null,
                    albumName = null,
                    groupId = groupId,
                    groupName = null)

            every { songService.getSongsByGroupId(any(), any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongsByGroupId(groupId, SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get songs with partial pagination query params and groupName" {
            val groupName = "name"
            val song = Arb.songRaw().single()
            val songsWithPages = DataWithPages<Song>(listOf(song), 0u)
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = null,
                    albumName = null,
                    groupId = null,
                    groupName = groupName)

            every { songService.getSongsByGroupName(any(), any(), any(), any(), any(), any()) } returns IO { songsWithPages }

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songsWithPages))

            verify(exactly = 1) { songService.getSongsByGroupName(groupName, SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)) }
        }

        "Get songs call handleUnexpectedException function" {
            val queryParams = SongQueryParams(
                    page = 0,
                    pageSize = null,
                    sort = null,
                    sortDir = "DESC",
                    albumId = null,
                    albumName = null,
                    groupId = null,
                    groupName = null
            )
            val exception = Exception("My exception")

            every { songService.getSongs(any(), any(), any(), any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Song>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            songController.getSongs(queryParams) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { songService.getSongs(SongRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Song>(exception) }
        }

        // Get song by id

        "Get song by id returns 200 status with the song song" {
            val songRaw = Arb.songRaw().single()

            every { songService.getSongById(any(), any()) } returns IO { Some(songRaw) }

            songController.getSongById(songRaw.id) shouldBe
                    ResponseEntity.ok(ResponseSuccess(songRaw))

            verify(exactly = 1) { songService.getSongById(songRaw.id, SongRaw::class) }
        }

        "Get song by id returns 404 status if the song song doesn't exist" {
            val songId = UUID.randomUUID()

            every { songService.getSongById(any(), any()) } returns IO { None }

            songController.getSongById(songId) shouldBe
                    ResponseEntity.notFound().build()

            verify(exactly = 1) { songService.getSongById(songId, SongRaw::class) }
        }

        "Get song by id call handleUnexpectedException function" {
            val songId = UUID.randomUUID()
            val exception = Exception("My exception")

            every { songService.getSongById(any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Song>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            songController.getSongById(songId) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { songService.getSongById(songId, SongRaw::class) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Song>(exception) }
        }
    }
}
