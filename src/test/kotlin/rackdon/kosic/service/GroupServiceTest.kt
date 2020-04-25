package rackdon.kosic.service

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.take
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.GroupRepositoryIOJpa
import rackdon.kosic.utils.generator.groupCreation
import rackdon.kosic.utils.generator.groupRaw
import java.util.UUID
import rackdon.kosic.model.Page as ServicePage

class GroupServiceTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest

    private val groupRepositoryMock = mockk<GroupRepositoryIOJpa>()
    private val groupService = GroupServiceIOJpa(groupRepositoryMock)

    init {
        "Create group returns IO of groupRaw" {
            val groupCreation = Arb.groupCreation().single()
            val ioGroupRaw = IO { Arb.groupRaw().single() }

            every { groupRepositoryMock.save(any()) } returns ioGroupRaw

            groupService.createGroup(groupCreation) shouldBe ioGroupRaw
            verify(exactly = 1) { groupRepositoryMock.save(groupCreation) }
        }

        "Get Groups is called projection and None values and return groups with pages" {
            val groupList = Arb.groupRaw().take(1).toList()
            val groupPage: Page<GroupRaw> = PageImpl(groupList)
            val ioGroupRaw = IO { groupPage }
            val projection = GroupRaw::class

            every { groupRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioGroupRaw

            val groupsResult = groupService.getGroups(projection, None, None, None, None).unsafeRunSync()

            groupsResult shouldBe DataWithPages(groupList, 1u)

            verify(exactly = 1) { groupRepositoryMock.findAll(projection, ServicePage(0u), PageSize(10u),
                    emptyList(), SortDir.DESC) }
        }

        "Get Groups is called with all values and return groups with pages" {
            val groupList = Arb.groupRaw().take(1).toList()
            val groupPage: Page<GroupRaw> = PageImpl(groupList)
            val ioGroupRaw = IO { groupPage }
            val projection = GroupRaw::class
            val page = ServicePage(2u)
            val pageSize = PageSize(20u)
            val sort = listOf("name")
            val sortDir = SortDir.ASC

            every { groupRepositoryMock.findAll(any(), any(), any(), any(), any()) } returns ioGroupRaw

            val groupsResult = groupService.getGroups(projection, Some(page), Some(pageSize), Some(sort), Some(sortDir)).unsafeRunSync()

            groupsResult shouldBe DataWithPages(groupList, 1u)
            verify(exactly = 1) { groupRepositoryMock.findAll(projection, page, pageSize, sort, sortDir) }
        }

        "Get plan group by id return IO option of the specified plan group projection" {
            val groupId = UUID.randomUUID()
            val response = IO { Option.just(Arb.groupRaw().single()) }
            val projection = GroupRaw::class

            every { groupRepositoryMock.findById(any(), any()) } returns response

            groupService.getGroupById(groupId, projection) shouldBe response
            verify(exactly = 1) { groupRepositoryMock.findById(groupId, projection) }
        }

        "Get plan group by name return IO option of the specified plan group projection" {
            val groupName = "name"
            val response = IO { Option.just(Arb.groupRaw().single()) }
            val projection = GroupRaw::class

            every { groupRepositoryMock.findByName(any(), any()) } returns response

            groupService.getGroupByName(groupName, projection) shouldBe response
            verify(exactly = 1) { groupRepositoryMock.findByName(groupName, projection) }
        }
    }
}
