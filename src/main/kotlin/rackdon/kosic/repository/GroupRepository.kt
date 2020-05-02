package rackdon.kosic.repository

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import java.util.UUID
import kotlin.reflect.KClass

interface GroupRepository<T, F, G> {
    fun save(groupCreation: GroupCreation): Kind<T, Group>
    fun findAll(projection: KClass<out Group>, page: Page = Page(), pageSize: PageSize = PageSize(),
            sort: List<String> = emptyList(), sortDir: SortDir = SortDir.DESC): Kind<F, Kind<G, Group>>
    fun findById(id: UUID, projection: KClass<out Group>): Kind<T, Option<Group>>
    fun findByName(name: String, projection: KClass<out Group>): Kind<T, Option<Group>>
}
