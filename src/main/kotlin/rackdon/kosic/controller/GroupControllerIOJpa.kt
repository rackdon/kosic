package rackdon.kosic.controller

import arrow.core.toOption
import arrow.fx.extensions.io.async.effectMap
import arrow.integrations.kotlinx.suspendCancellable
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rackdon.kosic.controller.dto.ApiResponse
import rackdon.kosic.controller.dto.GroupCreationDto
import rackdon.kosic.controller.dto.GroupQueryParams
import rackdon.kosic.controller.dto.ResponseError
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Group
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.GroupServiceIOJpa
import java.net.URI
import java.util.UUID
import javax.validation.Valid

@RestController
@Profile("test", "jpa", "local-jpa")
@RequestMapping("/api/groups")
class GroupControllerIOJpa(val groupService: GroupServiceIOJpa, val controllerExceptionHandler: ControllerExceptionHandler) :
    GroupController {

    @PostMapping("")
    override suspend fun createGroup(
            @RequestHeader headers: HttpHeaders,
            @RequestBody @Valid groupCreationDto: GroupCreationDto): ResponseEntity<out ApiResponse<Group>> {
        return groupService.createGroup(GroupCreationDto.toModelCreation(groupCreationDto))
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<Group>(it) },
                        { ResponseEntity.created(URI("${headers.host}/api/groups/${it.id}")).build() }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("")
    override suspend fun getGroups(@Valid queryParams: GroupQueryParams): ResponseEntity<out ApiResponse<DataWithPages<Group>>> {
        val page = queryParams.page?.let { Page(it.toUInt()) }.toOption()
        val pageSize = queryParams.pageSize?.let { PageSize(it.toUInt()) }.toOption()
        val sort = queryParams.sort.toOption()
        val sortDir = queryParams.sortDir?.let { SortDir.valueOf(it) }.toOption()
        return groupService.getGroups(GroupRaw::class, page, pageSize, sort, sortDir)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<DataWithPages<Group>>(it) },
                        { ResponseEntity.ok(ResponseSuccess(it)) }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("/{id}")
    override suspend fun getGroupById(@PathVariable("id") groupId: UUID): ResponseEntity<out ApiResponse<Group>> {
        return groupService.getGroupById(groupId, GroupRaw::class)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<Group>(it) },
                        { optionPlanGroup ->
                            optionPlanGroup.fold(
                                    { ResponseEntity.notFound().build<ResponseError<Group>>() },
                                    { ResponseEntity.ok(ResponseSuccess(it)) }) }
                )
            }
            .suspendCancellable()
    }
}
