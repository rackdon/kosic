package rackdon.kosic.repository

import arrow.core.ForId
import arrow.core.Id
import arrow.core.Option
import arrow.fx.reactor.FluxK
import arrow.fx.reactor.ForFluxK
import arrow.fx.reactor.ForMonoK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.k
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.repository.Pagination.paginated
import rackdon.kosic.repository.entity.mongo.GroupEntityMongo
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.reflect.KClass
import rackdon.kosic.model.Pagination as ModelPagination

@Profile("test", "mongo", "local-mongo")
interface GroupMongo : ReactiveMongoRepository<GroupEntityMongo, UUID> {
    fun findByName(name: String): Mono<GroupEntityMongo>
}

@Repository
@Profile("test", "mongo", "local-mongo")
class GroupRepositoryReactiveMongo(private val groupMongo: GroupMongo) : GroupRepository<ForMonoK, ForFluxK, ForId> {

    private fun getTransformer(projection: KClass<out Group>): (groupEntityMongo: GroupEntityMongo) -> Group {
        return { groupMongo -> when (projection) {
            GroupRaw::class -> groupMongo.toModelRaw()
            else -> groupMongo.toModelRaw()
        }
        }
    }

    override fun save(groupCreation: GroupCreation): MonoK<GroupRaw> {
        return groupMongo.save(GroupEntityMongo.fromCreation(groupCreation))
            .map { it.toModelRaw() }.k()
    }

    override fun findAll(projection: KClass<out Group>, pagination: ModelPagination): FluxK<Id<Group>> {
        val finalSort = Pagination.getSort(pagination.sort, pagination.sortDir)
        val transformer = getTransformer(projection)
        return groupMongo.findAll(finalSort)
            .paginated(pagination.page, pagination.pageSize)
            .map { Id.just(transformer(it)) }.k()
    }

    fun countAll(): MonoK<Int> {
        return groupMongo.count().map { it.toInt() }.k()
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
