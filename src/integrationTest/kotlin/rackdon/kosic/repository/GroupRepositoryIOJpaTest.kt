package rackdon.kosic.repository

import arrow.core.None
import arrow.core.Some
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.spring.SpringListener
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.entity.jpa.GroupEntityJpa
import rackdon.kosic.utils.DatabaseCleanerPsql
import rackdon.kosic.utils.FactoryJpa
import rackdon.kosic.utils.generator.groupCreation
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.EntityManager

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@DataJpaTest
class GroupRepositoryIOJpaTest(entityManager: EntityManager, groupJpa: GroupJpa) : StringSpec() {
    override fun listeners() = listOf(SpringListener)

    private val groupRepositoryJpa = GroupRepositoryIOJpa(groupJpa)
    private val databaseCleaner = DatabaseCleanerPsql(entityManager)
    private val factory = FactoryJpa(entityManager)

    override fun beforeTest(testCase: TestCase) {
        databaseCleaner.truncate()
    }

    init {

        "insert group should save the group in the database successfully" {
            val groupCreation = GroupCreation(name = "asdf", members = 4u, createdOn = LocalDateTime.now(),
                    dissolvedOn = null)
            val savedGroup = groupRepositoryJpa.save(groupCreation).unsafeRunSync()

            assertSoftly {
                savedGroup.name shouldBe groupCreation.name
                savedGroup.members shouldBe groupCreation.members
                savedGroup.createdOn shouldBe groupCreation.createdOn
                savedGroup.dissolvedOn shouldBe groupCreation.dissolvedOn
            }
        }

        "find all groups" {
            val group = factory.insertGroup()
            val result = groupRepositoryJpa.findAll(GroupRaw::class).unsafeRunSync()

            result.content shouldBe listOf(GroupEntityJpa.toModelRaw(group))
        }

        "find all groups sorted by name with default direction" {
            val group1 = factory.insertGroup(Arb.groupCreation(name = "a").single())
            val group2 = factory.insertGroup(Arb.groupCreation(name = "b").single())
            val result = groupRepositoryJpa.findAll(GroupRaw::class, sort = listOf("name")).unsafeRunSync()

            result.content shouldBe listOf(group2, group1).map { GroupEntityJpa.toModelRaw(it) }
        }

        "find all groups sorted by name with asc direction" {
            val group1 = factory.insertGroup(Arb.groupCreation(name = "a").single())
            val group2 = factory.insertGroup(Arb.groupCreation(name = "b").single())
            val result = groupRepositoryJpa.findAll(GroupRaw::class, sort = listOf("name"), sortDir = SortDir.ASC)
                .unsafeRunSync()

            result.content shouldBe listOf(group1, group2).map { GroupEntityJpa.toModelRaw(it) }
        }

        "find by id return correct group" {
            val group = factory.insertGroup()
            val result = groupRepositoryJpa.findById(group.id, GroupRaw::class).unsafeRunSync()

            result shouldBe Some(GroupEntityJpa.toModelRaw(group))
        }

        "find by id return None if not exists" {
            val result = groupRepositoryJpa.findById(UUID.randomUUID(), GroupRaw::class).unsafeRunSync()

            result shouldBe None
        }

        "find by name return correct group" {
            val group = factory.insertGroup()
            val result = groupRepositoryJpa.findByName(group.name, GroupRaw::class).unsafeRunSync()

            result shouldBe Some(GroupEntityJpa.toModelRaw(group))
        }

        "find by name return None if not exists" {
            val result = groupRepositoryJpa.findByName("non existent", GroupRaw::class).unsafeRunSync()

            result shouldBe None
        }
    }
}
