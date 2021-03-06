package rackdon.kosic.repository

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.Pagination
import java.util.UUID
import kotlin.reflect.KClass

interface AlbumRepository<T, F, G> {
    fun save(albumCreation: AlbumCreation): Kind<T, Album>
    fun findAll(projection: KClass<out Album>, pagination: Pagination): Kind<F, Kind<G, Album>>
    fun findById(id: UUID, projection: KClass<out Album>): Kind<T, Option<Album>>
    fun findByName(name: String, projection: KClass<out Album>): Kind<T, Option<Album>>
    fun findByGroupId(groupId: UUID, projection: KClass<out Album>, pagination: Pagination): Kind<F, Kind<G, Album>>
    fun findByGroupName(groupName: String, projection: KClass<out Album>, pagination: Pagination): Kind<F, Kind<G, Album>>
}
