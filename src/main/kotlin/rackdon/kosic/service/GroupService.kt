package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import java.util.UUID
import kotlin.reflect.KClass

interface GroupService<T> {
    fun createGroup(groupCreation: GroupCreation): Kind<T, Group>
    fun getGroups(projection: KClass<out Group>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): Kind<T, DataWithPages<Group>>
    fun getGroupById(id: UUID, projection: KClass<out Group>): Kind<T, Option<Group>>
    fun getGroupByName(name: String, projection: KClass<out Group>): Kind<T, Option<Group>>
}
