package rackdon.kosic.repository

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.jpa.GroupEntityJpa
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KClass
import rackdon.kosic.model.Page as GroupPage

@Repository
interface GroupJpa : JpaRepository<GroupEntityJpa, UUID> {
    fun findByName(name: String): Optional<GroupEntityJpa>
}

@Repository
class GroupRepositoryJpa(private val groupJpa: GroupJpa) : GroupRepository<ForIO> {
    private fun getPageRequest(page: GroupPage, pageSize: PageSize, sort: List<String>, sortDir: SortDir): PageRequest {
        val finalSort = if (sort.isEmpty()) Sort.unsorted() else Sort.by(Sort.Direction.valueOf(sortDir.name), *sort.toTypedArray())
        return PageRequest.of(page.value.toInt(), pageSize.value.toInt(), finalSort)
    }

    private fun getTransformer(projection: KClass<out Group>): (groupEntityJpa: GroupEntityJpa) -> Group {
        return { groupJpa -> when (projection) {
            GroupRaw::class -> GroupEntityJpa.toModelRaw(groupJpa)
            else -> GroupEntityJpa.toModelRaw(groupJpa)
        }
        }
    }

    override fun save(groupCreation: GroupCreation): IO<GroupRaw> {
        return IO {
            val group = groupJpa.save(GroupEntityJpa.fromCreation(groupCreation))
            GroupEntityJpa.toModelRaw(group)
        }
    }

    override fun findAll(projection: KClass<out Group>, page: GroupPage, pageSize: PageSize, sort: List<String>,
            sortDir: SortDir): IO<Page<out Group>> {
        val pageRequest = getPageRequest(page, pageSize, sort, sortDir)
        val transformer = getTransformer(projection)
        return IO { groupJpa.findAll(pageRequest).map { transformer(it) } }
    }

    override fun findById(id: UUID, projection: KClass<out Group>): IO<Option<Group>> {
        val transformer = getTransformer(projection)
        return IO { groupJpa.findById(id).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }

    override fun findByName(name: String, projection: KClass<out Group>): IO<Option<Group>> {
        val transformer = getTransformer(projection)
        return IO { groupJpa.findByName(name).map { Option.just(transformer(it)) }.orElse(Option.empty()) }
    }
}
