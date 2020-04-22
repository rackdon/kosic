package rackdon.kosic.repository

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.AlbumWithGroup
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.jpa.AlbumEntityJpa
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KClass
import rackdon.kosic.model.Page as AlbumPage

@Repository
interface AlbumJpa : JpaRepository<AlbumEntityJpa, UUID> {
    fun findByName(name: String): Optional<AlbumEntityJpa>
    fun findByGroupId(groupId: UUID, pageRequest: Pageable): Page<AlbumEntityJpa>
    fun findByGroupName(groupName: String, pageRequest: Pageable): Page<AlbumEntityJpa>
}

@Repository
class AlbumRepositoryJpa(private val albumJpa: AlbumJpa, private val groupJpa: GroupJpa) : AlbumRepository<ForIO> {

    private fun getPageRequest(page: AlbumPage, pageSize: PageSize, sort: List<String>, sortDir: SortDir): PageRequest {
        val finalSort = if (sort.isEmpty()) Sort.unsorted() else Sort.by(Sort.Direction.valueOf(sortDir.name), *sort.toTypedArray())
        return PageRequest.of(page.value.toInt(), pageSize.value.toInt(), finalSort)
    }

    private fun getTransformer(projection: KClass<out Album>): (albumEntityJpa: AlbumEntityJpa) -> Album {
        return { albumJpa -> when (projection) {
            AlbumRaw::class -> AlbumEntityJpa.toModelRaw(albumJpa)
            AlbumWithGroup::class -> AlbumEntityJpa.toModelWithGroup(albumJpa)
            else -> AlbumEntityJpa.toModelBase(albumJpa)
        }
        }
    }

    override fun save(albumCreation: AlbumCreation): IO<AlbumRaw> {
        return IO {
            val groupJpa = groupJpa.findById(albumCreation.groupId)
            groupJpa.map {
                val album = albumJpa.save(AlbumEntityJpa.fromCreation(albumCreation, it))
                AlbumEntityJpa.toModelRaw(album)
            }.orElseThrow { GroupNotFound }
        }
    }

    override fun findAll(projection: KClass<out Album>, page: AlbumPage, pageSize: PageSize, sort: List<String>,
            sortDir: SortDir): IO<Page<out Album>> {
        val pageRequest = getPageRequest(page, pageSize, sort, sortDir)
        val transformer = getTransformer(projection)
        return IO { albumJpa.findAll(pageRequest).map { transformer(it) } }
    }

    override fun findById(id: UUID, projection: KClass<out Album>): IO<Option<Album>> {
        val transformer = getTransformer(projection)
        return IO { albumJpa.findById(id).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }

    override fun findByName(name: String, projection: KClass<out Album>): IO<Option<Album>> {
        val transformer = getTransformer(projection)
        return IO { albumJpa.findByName(name).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }

    override fun findByGroupId(groupId: UUID, projection: KClass<out Album>, page: AlbumPage, pageSize: PageSize,
            sort: List<String>, sortDir: SortDir): IO<Page<out Album>> {
        val pageRequest = getPageRequest(page, pageSize, sort, sortDir)
        val transformer = getTransformer(projection)
        return IO { albumJpa.findByGroupId(groupId, pageRequest).map { transformer(it) } }
    }
    override fun findByGroupName(groupName: String, projection: KClass<out Album>, page: AlbumPage, pageSize: PageSize,
            sort: List<String>, sortDir: SortDir): IO<Page<out Album>> {
        val pageRequest = getPageRequest(page, pageSize, sort, sortDir)
        val finalProjection = getTransformer(projection)
        return IO { albumJpa.findByGroupName(groupName, pageRequest).map { finalProjection(it) } }
    }
}
