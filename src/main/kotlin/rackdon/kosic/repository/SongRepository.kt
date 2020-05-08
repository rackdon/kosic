package rackdon.kosic.repository

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import java.util.UUID
import kotlin.reflect.KClass

interface SongRepository<T, F, G> {
    fun save(songCreation: SongCreation): Kind<T, Song>
    fun findAll(projection: KClass<out Song>, pagination: Pagination): Kind<F, Kind<G, Song>>
    fun findById(id: UUID, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun findByName(name: String, projection: KClass<out Song>): Kind<T, Option<Song>>
    fun findByAlbumId(albumId: UUID, projection: KClass<out Song>, pagination: Pagination): Kind<F, Kind<G, Song>>
    fun findByAlbumName(albumName: String, projection: KClass<out Song>, pagination: Pagination): Kind<F, Kind<G, Song>>
    fun findByGroupId(groupId: UUID, projection: KClass<out Song>, pagination: Pagination): Kind<F, Kind<G, Song>>
    fun findByGroupName(groupName: String, projection: KClass<out Song>, pagination: Pagination): Kind<F, Kind<G, Song>>
}
