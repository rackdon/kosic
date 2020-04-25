package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.syntax.function.partially1
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.AlbumRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList
import org.springframework.data.domain.Page as JpaPage

class AlbumServiceIOJpa(private val albumRepository: AlbumRepositoryIOJpa) : AlbumService<ForIO, JpaPage<out Album>> {
    override val defaultPage = Page()
    override val defaultPageSize = PageSize()
    override val defaultSort = emptyList<String>()
    override val defaultSortDir = SortDir.DESC

    override fun createAlbum(albumCreation: AlbumCreation): Kind<ForIO, Album> {
        return albumRepository.save(albumCreation)
    }

    override fun getAlbums(projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        return IO.fx {
            val albums = !super.ensurePagination(albumRepository::findAll.partially1(projection), page, pageSize, sort, sortDir)
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
            val albums = !super.ensurePagination(albumRepository::findByGroupId.partially1(groupId)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }

    override fun getAlbumsByGroupName(groupName: String, projection: KClass<out Album>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        return IO.fx {
            val albums = !super.ensurePagination(albumRepository::findByGroupName.partially1(groupName)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }
}
