package rackdon.kosic.service

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.AlbumRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList

@Service
@Profile("test", "jpa", "local-jpa")
class AlbumServiceIOJpa(private val albumRepository: AlbumRepositoryIOJpa) : AlbumService<ForIO> {

    override fun createAlbum(albumCreation: AlbumCreation): IO<AlbumRaw> {
        return albumRepository.save(albumCreation)
    }

    override fun getAlbums(projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val albums = !albumRepository.findAll(projection, pagination)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }

    override fun getAlbumById(id: UUID, projection: KClass<out Album>): IO<Option<Album>> {
        return albumRepository.findById(id, projection)
    }

    override fun getAlbumByName(name: String, projection: KClass<out Album>): IO<Option<Album>> {
        return albumRepository.findByName(name, projection)
    }

    override fun getAlbumsByGroupId(groupId: UUID, projection: KClass<out Album>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val albums = !albumRepository.findByGroupId(groupId, projection, pagination)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }

    override fun getAlbumsByGroupName(groupName: String, projection: KClass<out Album>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val albums = !albumRepository.findByGroupName(groupName, projection, pagination)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }
}
