package rackdon.kosic.repository

import arrow.core.None
import arrow.core.Some
import arrow.core.value
import arrow.fx.reactor.unsafeRunSync
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.take
import io.kotest.spring.SpringListener
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.mongo.GroupEntityMongo
import rackdon.kosic.utils.DatabaseCleanerMongo
import rackdon.kosic.utils.FactoryMongo
import rackdon.kosic.utils.generator.groupCreation
import java.time.LocalDateTime
import java.util.UUID

@DataMongoTest
class GroupRepositoryReactiveMongoTest(groupMongo: GroupMongo, reactiveMongoTemplate: ReactiveMongoTemplate) :
    StringSpec() {
    override fun listeners() = listOf(SpringListener)

    private val groupRepositoryMongo = GroupRepositoryReactiveMongo(groupMongo)
    private val databaseCleaner = DatabaseCleanerMongo(reactiveMongoTemplate)
    private val factory = FactoryMongo(reactiveMongoTemplate)

    override fun beforeTest(testCase: TestCase) {
        databaseCleaner.truncate()
    }

    init {
        "insert group should save the group in the database successfully" {
            val groupCreation = GroupCreation(name = "asdf", members = 4u, createdOn = LocalDateTime.now(),
                    dissolvedOn = null)
            val savedGroup = groupRepositoryMongo.save(groupCreation).unsafeRunSync()!!
            factory.insertGroup()
            factory.insertGroup()

            assertSoftly {
                savedGroup.name shouldBe groupCreation.name
                savedGroup.members shouldBe groupCreation.members
                savedGroup.createdOn shouldBe groupCreation.createdOn
                savedGroup.dissolvedOn shouldBe groupCreation.dissolvedOn
            }
        }

        "find all groups" {
            val pagination = Pagination()
            val group = factory.insertGroup()
            val result = groupRepositoryMongo.findAll(GroupRaw::class, pagination)
                .flux
                .collectList()
                .block()

            result?.map { it.value() } shouldBe listOf(GroupEntityMongo.toModelRaw(group))
        }

        "find all groups sorted by name with default direction" {
            val pagination = Pagination(sort = listOf("name"))
            val group1 = factory.insertGroup(Arb.groupCreation(name = "a").single())
            val group2 = factory.insertGroup(Arb.groupCreation(name = "b").single())
            val result = groupRepositoryMongo.findAll(GroupRaw::class, pagination)
                .flux
                .collectList()
                .block()

            result?.map { it.value() } shouldBe listOf(group2, group1).map { GroupEntityMongo.toModelRaw(it) }
        }

        "find all groups sorted by name with asc direction" {
            val pagination = Pagination(sort = listOf("name"), sortDir = SortDir.ASC)
            val group1 = factory.insertGroup(Arb.groupCreation(name = "a").single())
            val group2 = factory.insertGroup(Arb.groupCreation(name = "b").single())
            val result = groupRepositoryMongo.findAll(GroupRaw::class, pagination)
                .flux
                .collectList()
                .block()

            result?.map { it.value() } shouldBe listOf(group1, group2).map { GroupEntityMongo.toModelRaw(it) }
        }

        "count all groups return the correct result" {
            Arb.groupCreation().take(3).forEach { factory.insertGroup(it) }

            val result = groupRepositoryMongo.countAll().unsafeRunSync()

            result shouldBe 3
        }

        "count all groups return 0 if no groups" {

            val result = groupRepositoryMongo.countAll().unsafeRunSync()

            result shouldBe 0
        }

        "find by id return correct group" {
            val group = factory.insertGroup()
            val result = groupRepositoryMongo.findById(group.id, GroupRaw::class).unsafeRunSync()

            result shouldBe Some(GroupEntityMongo.toModelRaw(group))
        }

        "find by id return None if not exists" {
            val result = groupRepositoryMongo.findById(UUID.randomUUID(), GroupRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by name return correct group" {
            val group = factory.insertGroup()
            val result = groupRepositoryMongo.findByName(group.name, GroupRaw::class).unsafeRunSync()

            result shouldBe Some(GroupEntityMongo.toModelRaw(group))
        }

        "find by name return None if not exists" {
            val result = groupRepositoryMongo.findByName("non existent", GroupRaw::class).unsafeRunSync()

            result shouldBe None
        }
    }
}
