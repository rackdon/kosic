package rackdon.kosic.repository

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import rackdon.kosic.model.AlbumNotFound
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SongWithAlbum
import rackdon.kosic.model.SongWithAlbumAndGroup
import rackdon.kosic.repository.entity.jpa.SongEntityJpa
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KClass
import rackdon.kosic.model.Pagination as ModelPagination

@Repository
@Profile("test", "jpa", "local-jpa")
interface SongJpa : JpaRepository<SongEntityJpa, UUID> {
    fun findByName(name: String): Optional<SongEntityJpa>
    fun findByAlbumId(albumId: UUID, pageRequest: Pageable): Page<SongEntityJpa>
    fun findByAlbumName(albumName: String, pageRequest: Pageable): Page<SongEntityJpa>
    @Query("SELECT s from SongEntityJpa s WHERE s.album.group.id = ?1")
    fun findByGroupId(groupId: UUID, pageRequest: Pageable): Page<SongEntityJpa>
    @Query("SELECT s from SongEntityJpa s WHERE s.album.group.name = ?1")
    fun findByGroupName(groupName: String, pageRequest: Pageable): Page<SongEntityJpa>
}

@Repository
@Profile("test", "jpa", "local-jpa")
class SongRepositoryIOJpa(private val songJpa: SongJpa, private val albumJpa: AlbumJpa) :
    SongRepository<ForIO, ForIO, ForPageK> {

    private fun getTransformer(projection: KClass<out Song>): (songEntityJpa: SongEntityJpa) -> Song {
        return { songJpa -> when (projection) {
            SongRaw::class -> songJpa.toModelRaw()
            SongWithAlbum::class -> songJpa.toModelWithAlbum()
            SongWithAlbumAndGroup::class -> songJpa.toModelWithAlbumAndGroup()
            else -> songJpa.toModelBase()
        }
        }
    }

    override fun save(songCreation: SongCreation): IO<SongRaw> {
        return IO {
            val albumJpa = albumJpa.findById(songCreation.albumId)
            albumJpa.map {
                val song = songJpa.save(SongEntityJpa.fromCreation(songCreation, it))
                song.toModelRaw()
            }.orElseThrow { AlbumNotFound }
        }
    }

    override fun findAll(projection: KClass<out Song>, pagination: ModelPagination): IO<PageK<out Song>> {
        val pageRequest = Pagination.getPageRequest(pagination)
        val transformer = getTransformer(projection)
        return IO { songJpa.findAll(pageRequest).map { transformer(it) }.k() }
    }

    override fun findById(id: UUID, projection: KClass<out Song>): IO<Option<Song>> {
        val transformer = getTransformer(projection)
        return IO { songJpa.findById(id).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }

    override fun findByName(name: String, projection: KClass<out Song>): IO<Option<Song>> {
        val transformer = getTransformer(projection)
        return IO { songJpa.findByName(name).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }

    override fun findByAlbumId(albumId: UUID, projection: KClass<out Song>, pagination: ModelPagination
    ): IO<PageK<out Song>> {
        val pageRequest = Pagination.getPageRequest(pagination)
        val transformer = getTransformer(projection)
        return IO { songJpa.findByAlbumId(albumId, pageRequest).map { transformer(it) }.k() }
    }
    override fun findByAlbumName(albumName: String, projection: KClass<out Song>, pagination: ModelPagination
    ): IO<PageK<out Song>> {
        val pageRequest = Pagination.getPageRequest(pagination)
        val finalProjection = getTransformer(projection)
        return IO { songJpa.findByAlbumName(albumName, pageRequest).map { finalProjection(it) }.k() }
    }

    override fun findByGroupId(groupId: UUID, projection: KClass<out Song>, pagination: ModelPagination
    ): IO<PageK<out Song>> {
        val pageRequest = Pagination.getPageRequest(pagination)
        val transformer = getTransformer(projection)
        return IO { songJpa.findByGroupId(groupId, pageRequest).map { transformer(it) }.k() }
    }
    override fun findByGroupName(groupName: String, projection: KClass<out Song>, pagination: ModelPagination
    ): IO<PageK<out Song>> {
        val pageRequest = Pagination.getPageRequest(pagination)
        val finalProjection = getTransformer(projection)
        return IO { songJpa.findByGroupName(groupName, pageRequest).map { finalProjection(it) }.k() }
    }
}
