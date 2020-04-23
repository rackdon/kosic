package rackdon.kosic.repository

import arrow.core.None
import arrow.core.Some
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.spring.SpringListener
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import rackdon.kosic.model.SongBase
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SongWithAlbum
import rackdon.kosic.model.AlbumNotFound
import rackdon.kosic.model.SongWithAlbumAndGroup
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.jpa.SongEntityJpa
import rackdon.kosic.repository.entity.jpa.AlbumEntityJpa
import rackdon.kosic.repository.entity.jpa.GroupEntityJpa
import rackdon.kosic.utils.DatabaseCleanerPsql
import rackdon.kosic.utils.FactoryJpa
import rackdon.kosic.utils.Generator
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@DataJpaTest
class SongRepositoryJpaTest(entityManager: EntityManager, songJpa: SongJpa, albumJpa: AlbumJpa) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    private val songRepositoryJpa = SongRepositoryJpa(songJpa, albumJpa)
    private val databaseCleaner = DatabaseCleanerPsql(entityManager)
    private val generator = Generator()
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
            val song = factory.insertSong()

            val result = songRepositoryJpa.findAll(SongRaw::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelRaw(song))
        }

        "find all songs returning songs with albums projection" {
            val song = factory.insertSong()
            val result = songRepositoryJpa.findAll(SongWithAlbum::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelWithAlbum(song))
        }

        "find all songs sorted by name with default direction and song base projection" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)

            val result = songRepositoryJpa.findAll(SongBase::class, sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { SongEntityJpa.toModelBase(it) }
        }

        "find all songs sorted by name with asc direction and song raw projection" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            val result = songRepositoryJpa.findAll(SongRaw::class, sort = listOf("name"), sortDir = SortDir.ASC).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { SongEntityJpa.toModelRaw(it) }
        }

        "find by id return correct song with base projection" {
            val song = factory.insertSong()
            val result = songRepositoryJpa.findById(song.id, projection = SongBase::class).unsafeRunSync()

            result shouldBe Some(SongEntityJpa.toModelBase(song))
        }

        "find by id return None if not exists" {
            val result = songRepositoryJpa.findById(UUID.randomUUID(), projection = SongRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by name return correct song with raw projection" {
            val song = factory.insertSong()
            val result = songRepositoryJpa.findByName(song.name, projection = SongRaw::class).unsafeRunSync()

            result shouldBe Some(SongEntityJpa.toModelRaw(song))
        }

        "find by name return None if not exists" {
            val result = songRepositoryJpa.findByName("non existent", projection = SongRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by album id" {
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumId(song1.album.id, SongWithAlbum::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelWithAlbum(song1))
        }

        "find by album id sorted by song name with default direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumId(album.id, SongBase::class, sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { SongEntityJpa.toModelBase(it) }
        }

        "find by album id sorted by song name with asc direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong()

            val result = songRepositoryJpa.findByAlbumId(album.id, SongWithAlbum::class,
                    sort = listOf("name"), sortDir = SortDir.ASC).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { SongEntityJpa.toModelWithAlbum(it) }
        }

        "find by album name" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum(generator.generateAlbumCreation("my name")))
            val song1 = factory.insertSong(album = album)
            factory.insertSong()
            val result = songRepositoryJpa.findByAlbumName("my name", SongRaw::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelRaw(song1))
        }

        "find by album name sorted by song name with default direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum(generator.generateAlbumCreation("my name")))
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByAlbumName("my name", SongBase::class,
                    sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { SongEntityJpa.toModelBase(it) }
        }

        "find by album name sorted by song name with asc direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum(generator.generateAlbumCreation("my name")))
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByAlbumName("my name", SongRaw::class,
                    sort = listOf("name"), sortDir = SortDir.ASC).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { SongEntityJpa.toModelRaw(it) }
        }

        "find by group id" {
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByGroupId(song1.album.group.id, SongWithAlbumAndGroup::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelWithAlbumAndGroup(song1))
        }
        "find by group id sorted by song name with default direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByGroupId(album.groupId, SongWithAlbumAndGroup::class,
                    sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { SongEntityJpa.toModelWithAlbumAndGroup(it) }
        }

        "find by group id sorted by song name with asc direction" {
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum())
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByGroupId(album.groupId, SongWithAlbumAndGroup::class,
                    sort = listOf("name"), sortDir = SortDir.ASC).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { SongEntityJpa.toModelWithAlbumAndGroup(it) }
        }

        "find by group name" {
            val song1 = factory.insertSong()
            factory.insertSong()
            val result = songRepositoryJpa.findByGroupName(song1.album.group.name, SongWithAlbumAndGroup::class).unsafeRunSync()

            result.content shouldBe listOf(SongEntityJpa.toModelWithAlbumAndGroup(song1))
        }

        "find by group name sorted by song name with default direction" {
            val group = GroupEntityJpa.toModelRaw(factory.insertGroup())
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum(group = group))
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByGroupName(group.name, SongWithAlbumAndGroup::class,
                    sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(song2, song1).map { SongEntityJpa.toModelWithAlbumAndGroup(it) }
        }

        "find by group name sorted by song name with asc direction" {
            val group = GroupEntityJpa.toModelRaw(factory.insertGroup())
            val album = AlbumEntityJpa.toModelRaw(factory.insertAlbum(group = group))
            val song1 = factory.insertSong(generator.generateSongCreation(name = "a"), album)
            val song2 = factory.insertSong(generator.generateSongCreation(name = "b"), album)
            factory.insertSong(generator.generateSongCreation())
            val result = songRepositoryJpa.findByGroupName(group.name, SongWithAlbumAndGroup::class,
                    sort = listOf("name"), sortDir = SortDir.ASC).unsafeRunSync()

            result.content shouldBe listOf(song1, song2).map { SongEntityJpa.toModelWithAlbumAndGroup(it) }
        }
    }
}
