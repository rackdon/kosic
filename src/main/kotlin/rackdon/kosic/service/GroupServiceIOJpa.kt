package rackdon.kosic.service

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import org.springframework.stereotype.Service
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.GroupRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList

@Service
class GroupServiceIOJpa(val groupRepository: GroupRepositoryIOJpa) : GroupService<ForIO> {

    override fun createGroup(groupCreation: GroupCreation): IO<GroupRaw> {
        return groupRepository.save(groupCreation)
    }

    override fun getGroups(projection: KClass<out Group>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Group>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val groups = !groupRepository.findAll(projection, pagination)
            DataWithPages(groups.get().toList(), groups.totalPages.toUInt())
        }
    }

    override fun getGroupById(id: UUID, projection: KClass<out Group>): IO<Option<Group>> {
        return groupRepository.findById(id, projection)
    }

    override fun getGroupByName(name: String, projection: KClass<out Group>): IO<Option<Group>> {
        return groupRepository.findByName(name, projection)
    }
}
