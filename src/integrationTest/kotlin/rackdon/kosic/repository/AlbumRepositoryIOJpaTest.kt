package rackdon.kosic.repository

import arrow.core.None
import arrow.core.Some
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.spring.SpringListener
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import rackdon.kosic.model.AlbumBase
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.AlbumWithGroup
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.utils.DatabaseCleanerPsql
import rackdon.kosic.utils.FactoryJpa
import rackdon.kosic.utils.generator.albumCreation
import rackdon.kosic.utils.generator.groupCreation
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@DataJpaTest
class AlbumRepositoryIOJpaTest(entityManager: EntityManager, albumJpa: AlbumJpa, groupJpa: GroupJpa) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    private val albumRepositoryJpa = AlbumRepositoryIOJpa(albumJpa, groupJpa)
    private val databaseCleaner = DatabaseCleanerPsql(entityManager)
    private val factory = FactoryJpa(entityManager)

    override fun beforeTest(testCase: TestCase) {
        databaseCleaner.truncate()
    }

    init {
        "insert album in the database if group exists" {
            val localDateTime = LocalDateTime.now()
            val group = factory.insertGroup()
            val albumCreation = AlbumCreation(name = "asdf", groupId = group.id, createdOn = localDateTime)
            val album = albumRepositoryJpa.save(albumCreation).unsafeRunSync()

            assertSoftly {
                album.name shouldBe albumCreation.name
                album.groupId shouldBe group.id
                album.createdOn shouldBe localDateTime
            }
        }

        "error is raised if group doesn't exist" {
            val localDateTime = LocalDateTime.now()
            val albumCreation = AlbumCreation(name = "asdf", groupId = UUID.randomUUID(), createdOn = localDateTime)
            val album = albumRepositoryJpa.save(albumCreation).attempt().unsafeRunSync()

            album shouldBeLeft GroupNotFound
        }

        "find all albums returning raw albums projection" {
            val pagination = Pagination()
            val album = factory.insertAlbum()

            val result = albumRepositoryJpa.findAll(AlbumRaw::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album.toModelRaw())
        }

        "find all albums returning albums with groups projection" {
            val pagination = Pagination()
            val album = factory.insertAlbum()
            val result = albumRepositoryJpa.findAll(AlbumWithGroup::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album.toModelWithGroup())
        }

        "find all albums sorted by name with default direction and album base projection" {
            val pagination = Pagination(sort = listOf("name"))
            val group = factory.insertGroup().toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)

            val result = albumRepositoryJpa.findAll(AlbumBase::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album2, album1).map { it.toModelBase() }
        }

        "find all albums sorted by name with asc direction and album raw projection" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val group = factory.insertGroup().toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)
            val result = albumRepositoryJpa.findAll(AlbumRaw::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album1, album2).map { it.toModelRaw() }
        }

        "find by id return correct album with base projection" {
            val album = factory.insertAlbum()
            val result = albumRepositoryJpa.findById(album.id, projection = AlbumBase::class).unsafeRunSync()

            result shouldBe Some(album.toModelBase())
        }

        "find by id return None if not exists" {
            val result = albumRepositoryJpa.findById(UUID.randomUUID(), projection = AlbumRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by name return correct album with raw projection" {
            val album = factory.insertAlbum()
            val result = albumRepositoryJpa.findByName(album.name, projection = AlbumRaw::class).unsafeRunSync()

            result shouldBe Some(album.toModelRaw())
        }

        "find by name return None if not exists" {
            val result = albumRepositoryJpa.findByName("non existent", projection = AlbumRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by group id" {
            val pagination = Pagination()
            val album1 = factory.insertAlbum()
            factory.insertAlbum()
            val result = albumRepositoryJpa.findByGroupId(album1.group.id, AlbumWithGroup::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album1.toModelWithGroup())
        }

        "find by group id sorted by album name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val group = factory.insertGroup().toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)
            factory.insertAlbum()
            val result = albumRepositoryJpa.findByGroupId(group.id, AlbumBase::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(album2, album1).map { it.toModelBase() }
        }

        "find by group id sorted by album name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val group = factory.insertGroup().toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)
            factory.insertAlbum()

            val result = albumRepositoryJpa.findByGroupId(group.id, AlbumWithGroup::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(album1, album2).map { it.toModelWithGroup() }
        }

        "find by group name" {
            val pagination = Pagination()
            val group = factory.insertGroup(Arb.groupCreation("my name").single()).toModelRaw()
            val album1 = factory.insertAlbum(group = group)
            factory.insertAlbum()
            val result = albumRepositoryJpa.findByGroupName("my name", AlbumRaw::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(album1.toModelRaw())
        }

        "find by group name sorted by album name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val group = factory.insertGroup(Arb.groupCreation("my name").single()).toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)
            factory.insertAlbum(Arb.albumCreation().single())
            val result = albumRepositoryJpa.findByGroupName("my name", AlbumBase::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(album2, album1).map { it.toModelBase() }
        }

        "find by group name sorted by album name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val group = factory.insertGroup(Arb.groupCreation("my name").single()).toModelRaw()
            val album1 = factory.insertAlbum(Arb.albumCreation(name = "a").single(), group)
            val album2 = factory.insertAlbum(Arb.albumCreation(name = "b").single(), group)
            factory.insertAlbum(Arb.albumCreation().single())
            val result = albumRepositoryJpa.findByGroupName("my name", AlbumRaw::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(album1, album2).map { it.toModelRaw() }
        }
    }
}
