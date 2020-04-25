package rackdon.kosic.controller

import arrow.core.None
import arrow.core.Some
import arrow.fx.IO
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import rackdon.kosic.controller.dto.GroupCreationDto
import rackdon.kosic.controller.dto.GroupQueryParams
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.GroupServiceIOJpa
import rackdon.kosic.utils.generator.groupCreationDto
import rackdon.kosic.utils.generator.groupRaw
import java.net.InetSocketAddress
import java.net.URI
import java.util.UUID

class GroupControllerIOJpaTest : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerTest
    private val groupService = mockk<GroupServiceIOJpa>()
    private val controllerExceptionHandler = mockk<ControllerExceptionHandler>()
    private val groupController = GroupControllerIOJpa(groupService, controllerExceptionHandler)

    init {
        // Create Group

        "Create group returns 201 status when all data is correct" {
            val groupCreationDto = Arb.groupCreationDto().single()
            val groupCreation = GroupCreationDto.toModelCreation(groupCreationDto)
            val groupRaw = Arb.groupRaw().single()
            val host = "localhost:8080"
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            headers.host = InetSocketAddress.createUnresolved("localhost", 8080)

            every { groupService.createGroup(any()) } returns IO { groupRaw }

            groupController.createGroup(headers, groupCreationDto) shouldBe
                    ResponseEntity.created(URI("$host/api/groups/${groupRaw.id}")).build()

            verify(exactly = 1) { groupService.createGroup(groupCreation) }
        }

        "Create group call handleUnexpectedException function" {
            val groupCreationDto = Arb.groupCreationDto().single()
            val groupCreation = GroupCreationDto.toModelCreation(groupCreationDto)
            val headers = HttpHeaders(LinkedMultiValueMap(emptyMap()))
            val exception = Exception("My exception")

            every { groupService.createGroup(any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Group>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            groupController.createGroup(headers, groupCreationDto) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { groupService.createGroup(groupCreation) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Group>(exception) }
        }

        // Get groups

        "Get groups without query params" {
            val groupsWithPages = DataWithPages<Group>(emptyList(), 0u)
            val queryParams = GroupQueryParams(null, null, null, null)
            every { groupService.getGroups(any(), any(), any(), any(), any()) } returns
                    IO { groupsWithPages }

            groupController.getGroups(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(groupsWithPages))

            verify(exactly = 1) { groupService.getGroups(GroupRaw::class, None, None, None, None) }
        }

        "Get groups with all query params" {
            val group = Arb.groupRaw().single()
            val groupsWithPages = DataWithPages<Group>(listOf(group), 0u)
            val queryParams = GroupQueryParams(0, 1, listOf("a", "b"), "DESC")

            every { groupService.getGroups(any(), any(), any(), any(), any()) } returns IO { groupsWithPages }

            groupController.getGroups(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(groupsWithPages))

            verify(exactly = 1) { groupService.getGroups(GroupRaw::class, Some(Page(0u)),
                    Some(PageSize(1u)), Some(listOf("a", "b")), Some(SortDir.DESC)
            ) }
        }

        "Get groups with partial query params" {
            val group = Arb.groupRaw().single()
            val groupsWithPages = DataWithPages<Group>(listOf(group), 0u)
            val queryParams = GroupQueryParams(0, null, null, "DESC")

            every { groupService.getGroups(any(), any(), any(), any(), any()) } returns IO { groupsWithPages }

            groupController.getGroups(queryParams) shouldBe
                    ResponseEntity.ok(ResponseSuccess(groupsWithPages))

            verify(exactly = 1) { groupService.getGroups(GroupRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
        }

        "Get groups call handleUnexpectedException function" {
            val queryParams = GroupQueryParams(0, null, null, "DESC")
            val exception = Exception("My exception")

            every { groupService.getGroups(any(), any(), any(), any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Group>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            groupController.getGroups(queryParams) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { groupService.getGroups(GroupRaw::class, Some(Page(0u)), None,
                    None, Some(SortDir.DESC)
            ) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Group>(exception) }
        }

        // Get group by id

        "Get group by id returns 200 status with the plan group" {
            val groupRaw = Arb.groupRaw().single()

            every { groupService.getGroupById(any(), any()) } returns IO { Some(groupRaw) }

            groupController.getGroupById(groupRaw.id) shouldBe
                    ResponseEntity.ok(ResponseSuccess(groupRaw))

            verify(exactly = 1) { groupService.getGroupById(groupRaw.id, GroupRaw::class) }
        }

        "Get group by id returns 404 status if the plan group doesn't exist" {
            val groupId = UUID.randomUUID()

            every { groupService.getGroupById(any(), any()) } returns IO { None }

            groupController.getGroupById(groupId) shouldBe
                    ResponseEntity.notFound().build()

            verify(exactly = 1) { groupService.getGroupById(groupId, GroupRaw::class) }
        }

        "Get group by id call handleUnexpectedException function" {
            val groupId = UUID.randomUUID()
            val exception = Exception("My exception")

            every { groupService.getGroupById(any(), any()) } returns IO { throw exception }
            every { controllerExceptionHandler.handleUnexpectedException<Group>(any()) } returns
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            groupController.getGroupById(groupId) shouldBe
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

            verify(exactly = 1) { groupService.getGroupById(groupId, GroupRaw::class) }
            verify(exactly = 1) { controllerExceptionHandler.handleUnexpectedException<Group>(exception) }
        }
    }
}
