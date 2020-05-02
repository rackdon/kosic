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
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.AlbumRepositoryIOJpa
import rackdon.kosic.repository.k
import rackdon.kosic.utils.generator.albumCreation
import rackdon.kosic.utils.generator.albumRaw
import java.util.UUID
import rackdon.kosic.model.Page as ServicePage

class AlbumServiceIOJpaTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest

    private val albumRepositoryMock = mockk<AlbumRepositoryIOJpa>()
    private val albumService = AlbumServiceIOJpa(albumRepositoryMock)

    init {
        "Create album returns IO of albumRaw" {
            val albumCreation = Arb.albumCreation().single()
            val ioAlbumRaw = IO { Arb.albumRaw().single() }

            every { albumRepositoryMock.save(any()) } returns ioAlbumRaw

            albumService.createAlbum(albumCreation) shouldBe ioAlbumRaw
            verify(exactly = 1) { albumRepositoryMock.save(albumCreation) }
        }

        "Get Albums is called with projection and None values and return albums with pages" {
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class

            every { albumRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbums(projection, None, None, None, None).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)

            verify(exactly = 1) { albumRepositoryMock.findAll(projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Albums is called with all values and return albums with pages" {
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { albumRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbums(projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)
            verify(exactly = 1) { albumRepositoryMock.findAll(projection, page, pageSize, sort, sortDir) }
        }

        "Get album by id return IO option of the specified album projection" {
            val albumId = UUID.randomUUID()
            val response = IO { Option.just(Arb.albumRaw().single()) }
            val projection = AlbumRaw::class

            every { albumRepositoryMock.findById(any(), any()) } returns response

            albumService.getAlbumById(albumId, projection) shouldBe response
            verify(exactly = 1) { albumRepositoryMock.findById(albumId, projection) }
        }

        "Get album by name return IO option of the specified album projection" {
            val albumName = "name"
            val response = IO { Option.just(Arb.albumRaw().single()) }
            val projection = AlbumRaw::class

            every { albumRepositoryMock.findByName(any(), any()) } returns response

            albumService.getAlbumByName(albumName, projection) shouldBe response
            verify(exactly = 1) { albumRepositoryMock.findByName(albumName, projection) }
        }

        "Get Albums by group id is called projection and None values and return albums with pages" {
            val groupId = UUID.randomUUID()
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class

            every { albumRepositoryMock.findByGroupId(any(), any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbumsByGroupId(groupId, projection, None, None, None, None).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)

            verify(exactly = 1) { albumRepositoryMock.findByGroupId(groupId, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Albums by group id is called with all values and return albums with pages" {
            val groupId = UUID.randomUUID()
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { albumRepositoryMock.findByGroupId(any(), any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbumsByGroupId(groupId, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)
            verify(exactly = 1) { albumRepositoryMock.findByGroupId(groupId, projection, page, pageSize, sort, sortDir) }
        }

        "Get Albums by group name is called projection and None values and return albums with pages" {
            val groupName = "group name"
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class

            every { albumRepositoryMock.findByGroupName(any(), any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbumsByGroupName(groupName, projection, None, None, None, None).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)

            verify(exactly = 1) { albumRepositoryMock.findByGroupName(groupName, projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Albums by group name is called with all values and return albums with pages" {
            val groupName = "group name"
            val albumList = Arb.albumRaw().take(1).toList()
            val albumPage: Page<AlbumRaw> = PageImpl(albumList)
            val ioAlbumRaw = IO { albumPage.k() }
            val projection = AlbumRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { albumRepositoryMock.findByGroupName(any(), any(), any(), any(), any(), any()) } returns ioAlbumRaw

            val albumsResult = albumService.getAlbumsByGroupName(groupName, projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            albumsResult shouldBe DataWithPages(albumList, 1u)
            verify(exactly = 1) { albumRepositoryMock.findByGroupName(groupName, projection, page, pageSize, sort, sortDir) }
        }
    }
}
