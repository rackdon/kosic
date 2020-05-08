package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SortDir
import java.util.UUID
import kotlin.reflect.KClass

interface SongService<T> {
    fun createSong(songCreation: SongCreation): Kind<T, Song>
    fun getSongs(projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, DataWithPages<Song>>
    fun getSongById(id: UUID, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun getSongByName(name: String, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun getSongsByAlbumId(albumId: UUID, projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, DataWithPages<Song>>
    fun getSongsByAlbumName(albumName: String, projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, DataWithPages<Song>>
    fun getSongsByGroupId(groupId: UUID, projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, DataWithPages<Song>>
    fun getSongsByGroupName(groupName: String, projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, DataWithPages<Song>>
}
