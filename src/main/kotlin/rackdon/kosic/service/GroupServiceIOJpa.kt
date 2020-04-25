package rackdon.kosic.service

import arrow.core.Option
import arrow.core.getOrElse
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import org.springframework.stereotype.Service
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.GroupRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList

@Service
class GroupServiceIOJpa(val groupRepository: GroupRepositoryIOJpa) : GroupService<ForIO> {
    private val defaultPage = Page()
    private val defaultPageSize = PageSize()
    private val defaultSort = emptyList<String>()
    private val defaultSortDir = SortDir.DESC

    override fun createGroup(groupCreation: GroupCreation): IO<Group> {
        return groupRepository.save(groupCreation)
    }

    override fun getGroups(projection: KClass<out Group>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>): IO<DataWithPages<Group>> {
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return IO.fx {
            val groups = !groupRepository.findAll(projection, finalPage, finalPageSize, finalSort, finalSortDir)
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
