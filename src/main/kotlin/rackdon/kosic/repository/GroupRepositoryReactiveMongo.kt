package rackdon.kosic.repository

import arrow.core.ForId
import arrow.core.Id
import arrow.core.Option
import arrow.fx.reactor.FluxK
import arrow.fx.reactor.ForFluxK
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.k
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.mongo.GroupEntityMongo
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.reflect.KClass

@Repository
interface GroupMongo : ReactiveMongoRepository<GroupEntityMongo, UUID> {
    fun findByName(name: String): Mono<GroupEntityMongo>
}

class GroupRepositoryReactiveMongo(private val groupMongo: GroupMongo) :
    GroupRepository<ForMonoK, ForFluxK, ForId>, PaginationRepository {

    private fun getTransformer(projection: KClass<out Group>): (groupEntityMongo: GroupEntityMongo) -> Group {
        return { groupMongo -> when (projection) {
            GroupRaw::class -> GroupEntityMongo.toModelRaw(groupMongo)
            else -> GroupEntityMongo.toModelRaw(groupMongo)
        }
        }
    }

    override fun save(groupCreation: GroupCreation): MonoK<GroupRaw> {
        return groupMongo.save(GroupEntityMongo.fromCreation(groupCreation))
            .map { GroupEntityMongo.toModelRaw(it) }.k()
    }

    override fun findAll(projection: KClass<out Group>, page: Page, pageSize: PageSize, sort: List<String>,
            sortDir: SortDir): FluxK<Id<Group>> {
        val finalSort = getSort(sort, sortDir)
        val transformer = getTransformer(projection)
        return groupMongo.findAll(finalSort)
            .paginated(page, pageSize)
            .map { Id.just(transformer(it)) }.k()
    }

    override fun findById(id: UUID, projection: KClass<out Group>): MonoK<Option<Group>> {
        val transformer = getTransformer(projection)
        return groupMongo.findById(id).map { Option.just(transformer(it)) }.defaultIfEmpty(Option.empty()).k()
    }

    override fun findByName(name: String, projection: KClass<out Group>): MonoK<Option<Group>> {
        val transformer = getTransformer(projection)
        return groupMongo.findByName(name).map { Option.just(transformer(it)) }.defaultIfEmpty(Option.empty()).k()
    }
}