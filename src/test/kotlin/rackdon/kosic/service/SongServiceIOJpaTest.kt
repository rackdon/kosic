package rackdon.kosic.service

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.take
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.SongRepositoryIOJpa
import rackdon.kosic.utils.generator.songCreation
import rackdon.kosic.utils.generator.songRaw
import java.util.UUID
import rackdon.kosic.model.Page as ServicePage

class SongServiceIOJpaTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest

    private val songRepositoryMock = mockk<SongRepositoryIOJpa>()
    private val songService = SongServiceIOJpa(songRepositoryMock)

    init {
        "Create song returns IO of songRaw" {
            val songCreation = Arb.songCreation().single()
            val ioSongRaw = IO { Arb.songRaw().single() }

            every { songRepositoryMock.save(any()) } returns ioSongRaw

            songService.createSong(songCreation) shouldBe ioSongRaw
            verify(exactly = 1) { songRepositoryMock.save(songCreation) }
        }

        "Get Songs is called with projection and None values and return songs with pages" {
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class

            every { songRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongs(projection, None, None, None, None).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)

            verify(exactly = 1) { songRepositoryMock.findAll(projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Songs is called with all values and return songs with pages" {
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { songRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongs(projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)
            verify(exactly = 1) { songRepositoryMock.findAll(projection, page, pageSize, sort, sortDir) }
        }

        "Get song by id return IO option of the specified song projection" {
            val songId = UUID.randomUUID()
            val response = IO { Option.just(Arb.songRaw().single()) }
            val projection = SongRaw::class

            every { songRepositoryMock.findById(any(), any()) } returns response

            songService.getSongById(songId, projection) shouldBe response
            verify(exactly = 1) { songRepositoryMock.findById(songId, projection) }
        }

        "Get song by name return IO option of the specified song projection" {
            val songName = "name"
            val response = IO { Option.just(Arb.songRaw().single()) }
            val projection = SongRaw::class

            every { songRepositoryMock.findByName(any(), any()) } returns response

            songService.getSongByName(songName, projection) shouldBe response
            verify(exactly = 1) { songRepositoryMock.findByName(songName, projection) }
        }

        "Get Songs by album id is called projection and None values and return songs with pages" {
            val albumId = UUID.randomUUID()
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class

            every { songRepositoryMock.findByAlbumId(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByAlbumId(albumId, projection, None, None, None, None).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)

            verify(exactly = 1) { songRepositoryMock.findByAlbumId(albumId, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Songs by album id is called with all values and return songs with pages" {
            val albumId = UUID.randomUUID()
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { songRepositoryMock.findByAlbumId(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByAlbumId(albumId, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)
            verify(exactly = 1) { songRepositoryMock.findByAlbumId(albumId, projection, page, pageSize, sort, sortDir) }
        }

        "Get Songs by album name is called projection and None values and return songs with pages" {
            val albumName = "album name"
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class

            every { songRepositoryMock.findByAlbumName(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByAlbumName(albumName, projection, None, None, None, None).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)

            verify(exactly = 1) { songRepositoryMock.findByAlbumName(albumName, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Songs by album name is called with all values and return songs with pages" {
            val albumName = "album name"
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { songRepositoryMock.findByAlbumName(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByAlbumName(albumName, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)
            verify(exactly = 1) { songRepositoryMock.findByAlbumName(albumName, projection, page, pageSize, sort, sortDir) }
        }

        "Get Songs by group id is called projection and None values and return songs with pages" {
            val groupId = UUID.randomUUID()
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class

            every { songRepositoryMock.findByGroupId(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByGroupId(groupId, projection, None, None, None, None).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)

            verify(exactly = 1) { songRepositoryMock.findByGroupId(groupId, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Songs by group id is called with all values and return songs with pages" {
            val groupId = UUID.randomUUID()
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { songRepositoryMock.findByGroupId(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByGroupId(groupId, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)
            verify(exactly = 1) { songRepositoryMock.findByGroupId(groupId, projection, page, pageSize, sort, sortDir) }
        }

        "Get Songs by group name is called projection and None values and return songs with pages" {
            val groupName = "group name"
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class

            every { songRepositoryMock.findByGroupName(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByGroupName(groupName, projection, None, None, None, None).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)

            verify(exactly = 1) { songRepositoryMock.findByGroupName(groupName, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Songs by group name is called with all values and return songs with pages" {
            val groupName = "group name"
            val songList = Arb.songRaw().take(1).toList()
            val songPage: Page<SongRaw> = PageImpl(songList)
            val ioSongRaw = IO { songPage }
            val projection = SongRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { songRepositoryMock.findByGroupName(any(), any(), any(), any(), any(), any()) } returns ioSongRaw

            val songsResult = songService.getSongsByGroupName(groupName, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            songsResult shouldBe DataWithPages(songList, 1u)
            verify(exactly = 1) { songRepositoryMock.findByGroupName(groupName, projection, page, pageSize, sort, sortDir) }
        }
    }
}
