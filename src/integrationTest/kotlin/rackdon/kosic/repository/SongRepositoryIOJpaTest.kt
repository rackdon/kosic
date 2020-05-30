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
import rackdon.kosic.model.AlbumNotFound
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SongBase
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SongWithAlbum
import rackdon.kosic.model.SongWithAlbumAndGroup
import rackdon.kosic.model.SortDir
import rackdon.kosic.utils.DatabaseCleanerPsql
import rackdon.kosic.utils.FactoryJpa
import rackdon.kosic.utils.generator.albumCreation
import rackdon.kosic.utils.generator.songCreation
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@DataJpaTest
class SongRepositoryIOJpaTest(entityManager: EntityManager, songJpa: SongJpa, albumJpa: AlbumJpa) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    private val songRepositoryJpa = SongRepositoryIOJpa(songJpa, albumJpa)
    private val databaseCleaner = DatabaseCleanerPsql(entityManager)
    private val factory = FactoryJpa(entityManager)

    override fun beforeTest(testCase: TestCase) {
        databaseCleaner.truncate()
    }

    init {
        "insert song in the database if album exists" {
            val localDateTime = LocalDateTime.now()
            val album = factory.insertAlbum()
            val songCreation = SongCreation(name = "asdf", albumId = album.id, duration = 10u,
                    createdOn = localDateTime, meta = emptyMap())
            val song = songRepositoryJpa.save(songCreation).unsafeRunSync()

            assertSoftly {
                song.name shouldBe songCreation.name
                song.albumId shouldBe album.id
                song.createdOn shouldBe localDateTime
            }
        }

        "error is raised if album doesn't exist" {
            val localDateTime = LocalDateTime.now()
            val songCreation = SongCreation(name = "asdf", albumId = UUID.randomUUID(), duration = 10u,
                    createdOn = localDateTime, meta = emptyMap())
            val song = songRepositoryJpa.save(songCreation).attempt().unsafeRunSync()

            song shouldBeLeft AlbumNotFound
        }

        "find all songs returning raw songs projection" {
            val pagination = Pagination()
            val song = factory.insertSong()

            val result = songRepositoryJpa.findAll(SongRaw::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(song.toModelRaw())
        }

        "find all songs returning songs with albums projection" {
            val pagination = Pagination()
            val song = factory.insertSong()
            val result = songRepositoryJpa.findAll(SongWithAlbum::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(song.toModelWithAlbum())
        }

        "find all songs sorted by name with default direction and song base projection" {
            val pagination = Pagination(sort = listOf("name"))
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)

            val result = songRepositoryJpa.findAll(SongBase::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { it.toModelBase() }
        }

        "find all songs sorted by name with asc direction and song raw projection" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            val result = songRepositoryJpa.findAll(SongRaw::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { it.toModelRaw() }
        }

        "find by id return correct song with base projection" {
            val song = factory.insertSong()
            val result = songRepositoryJpa.findById(song.id, projection = SongBase::class).unsafeRunSync()

            result shouldBe Some(song.toModelBase())
        }

        "find by id return None if not exists" {
            val result = songRepositoryJpa.findById(UUID.randomUUID(), projection = SongRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by name return correct song with raw projection" {
            val song = factory.insertSong()
            val result = songRepositoryJpa.findByName(song.name, projection = SongRaw::class).unsafeRunSync()

            result shouldBe Some(song.toModelRaw())
        }

        "find by name return None if not exists" {
            val result = songRepositoryJpa.findByName("non existent", projection = SongRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by album id" {
            val pagination = Pagination()
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumId(song1.album.id, SongWithAlbum::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1.toModelWithAlbum())
        }

        "find by album id sorted by song name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumId(album.id, SongBase::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { it.toModelBase() }
        }

        "find by album id sorted by song name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong()

            val result = songRepositoryJpa.findByAlbumId(album.id, SongWithAlbum::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { it.toModelWithAlbum() }
        }

        "find by album name" {
            val pagination = Pagination()
            val album = factory.insertAlbum(Arb.albumCreation("my name").single()).toModelRaw()
            val song1 = factory.insertSong(album = album)
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumName("my name", SongRaw::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1.toModelRaw())
        }

        "find by album name sorted by song name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val album = factory.insertAlbum(Arb.albumCreation("my name").single()).toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByAlbumName("my name", SongBase::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { it.toModelBase() }
        }

        "find by album name sorted by song name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val album = factory.insertAlbum(Arb.albumCreation("my name").single()).toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByAlbumName("my name", SongRaw::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { it.toModelRaw() }
        }

        "find by group id" {
            val pagination = Pagination()
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByGroupId(song1.album.group.id, SongWithAlbumAndGroup::class,
            pagination).unsafeRunSync()

            result.content shouldBe listOf(song1.toModelWithAlbumAndGroup())
        }
        "find by group id sorted by song name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByGroupId(album.groupId, SongWithAlbumAndGroup::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { it.toModelWithAlbumAndGroup() }
        }

        "find by group id sorted by song name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val album = factory.insertAlbum().toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByGroupId(album.groupId, SongWithAlbumAndGroup::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { it.toModelWithAlbumAndGroup() }
        }

        "find by group name" {
            val pagination = Pagination()
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByGroupName(song1.album.group.name,
                    SongWithAlbumAndGroup::class, pagination).unsafeRunSync()

            result.content shouldBe listOf(song1.toModelWithAlbumAndGroup())
        }

        "find by group name sorted by song name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val group = factory.insertGroup().toModelRaw()
            val album = factory.insertAlbum(group = group).toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByGroupName(group.name, SongWithAlbumAndGroup::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { it.toModelWithAlbumAndGroup() }
        }

        "find by group name sorted by song name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val group = factory.insertGroup().toModelRaw()
            val album = factory.insertAlbum(group = group).toModelRaw()
            val song1 = factory.insertSong(Arb.songCreation(name = "a").single(), album)
            val song2 = factory.insertSong(Arb.songCreation(name = "b").single(), album)
            factory.insertSong(Arb.songCreation().single())
            val result = songRepositoryJpa.findByGroupName(group.name, SongWithAlbumAndGroup::class,
                    pagination).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { it.toModelWithAlbumAndGroup() }
        }
    }
}
