package rackdon.kosic.repository

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SortDir
import java.util.UUID
import kotlin.reflect.KClass

interface SongRepository<T> {
    fun save(songCreation: SongCreation): Kind<T, Song>
    fun findAll(projection: KClass<out Song>, page: Page = Page(), pageSize: PageSize = PageSize(),
            sort: List<String> = emptyList(), sortDir: SortDir = SortDir.DESC): Kind<T, Iterable<Song>>
    fun findById(id: UUID, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun findByName(name: String, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun findByAlbumId(albumId: UUID, projection: KClass<out Song>, page: Page = Page(), pageSize: PageSize = PageSize(),
            sort: List<String> = emptyList(), sortDir: SortDir = SortDir.DESC): Kind<T, Iterable<Song>>
    fun findByAlbumName(albumName: String, projection: KClass<out Song>, page: Page = Page(), pageSize: PageSize = PageSize(),
            sort: List<String> = emptyList(), sortDir: SortDir = SortDir.DESC): Kind<T, Iterable<Song>>

    // TODO find by group id and group name
}