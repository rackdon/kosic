package rackdon.kosic.service

import arrow.core.Id
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.reactor.FluxK
import arrow.fx.reactor.MonoK
import arrow.fx.reactor.unsafeRunSync
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.GroupRepositoryReactiveMongo
import rackdon.kosic.utils.generator.groupCreation
import rackdon.kosic.utils.generator.groupRaw
import java.util.UUID

class GroupServiceReactiveMongoTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest

    private val groupRepositoryMock = mockk<GroupRepositoryReactiveMongo>()
    private val groupService = GroupServiceReactiveMongo(groupRepositoryMock)

    init {
        "Create group returns MonoK of Group Raw" {
            val groupCreation = Arb.groupCreation().single()
            val monoKGroupRaw = MonoK { Arb.groupRaw().single() }

            every { groupRepositoryMock.save(any()) } returns monoKGroupRaw

            groupService.createGroup(groupCreation) shouldBe monoKGroupRaw
            verify(exactly = 1) { groupRepositoryMock.save(groupCreation) }
        }

        "Get Groups is called projection and None values and return groups with pages" {
            val group = Arb.groupRaw().single()
            val fluxKGroupRaw = FluxK { Id.just(group) }
            val projection = GroupRaw::class
            val pagination = Pagination()
            val countResult = 1

            every { groupRepositoryMock.findAll(any(), any()) } returns fluxKGroupRaw
            every { groupRepositoryMock.countAll() } returns MonoK.just(countResult)

            val groupsResult = groupService.getGroups(projection, None, None, None, None).unsafeRunSync()

            groupsResult shouldBe DataWithPages(listOf(group), countResult.toUInt())

            verify(exactly = 1) { groupRepositoryMock.findAll(projection, pagination) }
            verify(exactly = 1) { groupRepositoryMock.countAll() }
        }

        "Get Groups is called with all values and return groups with pages" {
            val group = Arb.groupRaw().single()
            val fluxKGroupRaw = FluxK { Id.just(group) }
            val projection = GroupRaw::class
            val countResult = 1
            val page = rackdon.kosic.model.Page(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC
            val pagination = Pagination(page, pageSize, sort, sortDir)

            every { groupRepositoryMock.findAll(any(), any()) } returns fluxKGroupRaw
            every { groupRepositoryMock.countAll() } returns MonoK.just(countResult)

            val groupsResult = groupService.getGroups(projection, Some(page), Some(pageSize),
                    Some(sort), Some(sortDir)).unsafeRunSync()

            groupsResult shouldBe DataWithPages(listOf(group), countResult.toUInt())

            verify(exactly = 1) { groupRepositoryMock.findAll(projection, pagination) }
            verify(exactly = 1) { groupRepositoryMock.countAll() }
        }

        "Get group by id return MonoK option of the specified group projection" {
            val groupId = UUID.randomUUID()
            val response = MonoK { Option.just(Arb.groupRaw().single()) }
            val projection = GroupRaw::class

            every { groupRepositoryMock.findById(any(), any()) } returns response

            groupService.getGroupById(groupId, projection) shouldBe response
            verify(exactly = 1) { groupRepositoryMock.findById(groupId, projection) }
        }

        "Get group by name return MonoK option of the specified group projection" {
            val groupName = "name"
            val response = MonoK { Option.just(Arb.groupRaw().single()) }
            val projection = GroupRaw::class

            every { groupRepositoryMock.findByName(any(), any()) } returns response

            groupService.getGroupByName(groupName, projection) shouldBe response
            verify(exactly = 1) { groupRepositoryMock.findByName(groupName, projection) }
        }
    }
}
