package rackdon.kosic.service

import arrow.core.Option
import arrow.core.value
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.k
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.GroupRepositoryReactiveMongo
import rackdon.kosic.utils.toIO
import rackdon.kosic.utils.toMonoK
import java.util.UUID
import kotlin.reflect.KClass

class GroupServiceReactiveMongo(private val groupRepository: GroupRepositoryReactiveMongo) : GroupService<ForMonoK> {

    override fun createGroup(groupCreation: GroupCreation): MonoK<GroupRaw> {
        return groupRepository.save(groupCreation)
    }

    override fun getGroups(projection: KClass<out Group>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): MonoK<DataWithPages<Group>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val groupsIO = groupRepository.findAll(projection, pagination).flux.collectList().k().toIO()
            val groupsCountIO = groupRepository.countAll().toIO()
            !IO.parMapN(groupsIO, groupsCountIO) {(g, c) ->
                DataWithPages(g.map { it.value() }, c.toUInt())}
        }.toMonoK()
    }

    override fun getGroupById(id: UUID, projection: KClass<out Group>): MonoK<Option<Group>> {
        return groupRepository.findById(id, projection)
    }

    override fun getGroupByName(name: String, projection: KClass<out Group>): MonoK<Option<Group>> {
        return groupRepository.findByName(name, projection)
    }
}
