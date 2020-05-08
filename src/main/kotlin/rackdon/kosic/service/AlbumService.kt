package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import java.util.UUID
import kotlin.reflect.KClass

interface AlbumService<T> {
    fun createAlbum(albumCreation: AlbumCreation): Kind<T, Album>
    fun getAlbums(projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): Kind<T, DataWithPages<Album>>
    fun getAlbumById(id: UUID, projection: KClass<out Album>): Kind<T, Option<Album>>
    fun getAlbumByName(name: String, projection: KClass<out Album>): Kind<T, Option<Album>>
    fun getAlbumsByGroupId(groupId: UUID, projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): Kind<T, DataWithPages<Album>>
    fun getAlbumsByGroupName(groupName: String, projection: KClass<out Album>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): Kind<T, DataWithPages<Album>>
}
