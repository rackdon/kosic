package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import arrow.core.getOrElse
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
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

class AlbumServiceIOJpa(val albumRepository: AlbumRepositoryIOJpa): AlbumService<ForIO> {
    private val defaultPage = Page()
    private val defaultPageSize = PageSize()
    private val defaultSort = emptyList<String>()
    private val defaultSortDir = SortDir.DESC

    override fun createAlbum(albumCreation: AlbumCreation): Kind<ForIO, Album> {
        return albumRepository.save(albumCreation)
    }

    override fun getAlbums(projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return IO.fx {
            val albums = !albumRepository.findAll(projection, finalPage, finalPageSize, finalSort, finalSortDir)
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
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return IO.fx {
            val albums = !albumRepository.findByGroupId(groupId, projection, finalPage, finalPageSize, finalSort, finalSortDir)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }

    override fun getAlbumsByGroupName(groupName: String, projection: KClass<out Album>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Album>> {
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return IO.fx {
            val albums = !albumRepository.findByGroupName(groupName, projection, finalPage, finalPageSize, finalSort, finalSortDir)
            DataWithPages(albums.get().toList(), albums.totalPages.toUInt())
        }
    }
}