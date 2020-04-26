package rackdon.kosic.controller

import arrow.core.toOption
import arrow.fx.extensions.io.async.effectMap
import arrow.integrations.kotlinx.suspendCancellable
import arrow.syntax.function.partially1
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rackdon.kosic.controller.dto.AlbumCreationDto
import rackdon.kosic.controller.dto.AlbumQueryParams
import rackdon.kosic.controller.dto.ApiResponse
import rackdon.kosic.controller.dto.ResponseError
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.Album
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.AlbumServiceIOJpa
import java.net.URI
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/albums")
class AlbumControllerIOJpa(val albumService: AlbumServiceIOJpa, val controllerExceptionHandler: ControllerExceptionHandler) :
    AlbumController {

    @PostMapping("")
    override suspend fun createAlbum(
            @RequestHeader headers: HttpHeaders,
            @RequestBody @Valid albumCreationDto: AlbumCreationDto
    ): ResponseEntity<out ApiResponse<Album>> {
        return albumService.createAlbum(AlbumCreationDto.toModelCreation(albumCreationDto))
            .attempt()
            .effectMap { result ->
                result.fold(
                        { when (it) {
                            is GroupNotFound -> {
                                val apiResponse = ResponseError<Album>(listOf(it.message))
                                ResponseEntity.badRequest().body(apiResponse)
                            }
                            else -> controllerExceptionHandler.handleUnexpectedException(it)
                        } },
                        { ResponseEntity.created(URI("${headers.host}/api/albums/${it.id}")).build() }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("")
    override suspend fun getAlbums(@Valid queryParams: AlbumQueryParams): ResponseEntity<out ApiResponse<DataWithPages<Album>>> {
        val page = queryParams.page?.let { Page(it.toUInt()) }.toOption()
        val pageSize = queryParams.pageSize?.let { PageSize(it.toUInt()) }.toOption()
        val sort = queryParams.sort.toOption()
        val sortDir = queryParams.sortDir?.let { SortDir.valueOf(it) }.toOption()
        val queryFunction = when {
            queryParams.groupId != null -> albumService::getAlbumsByGroupId.partially1(queryParams.groupId)
            queryParams.groupName != null -> albumService::getAlbumsByGroupName.partially1(queryParams.groupName)
            else -> albumService::getAlbums
        }
        return queryFunction(AlbumRaw::class, page, pageSize, sort, sortDir)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<DataWithPages<Album>>(it) },
                        { ResponseEntity.ok(ResponseSuccess(it)) }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("/{id}")
    override suspend fun getAlbumById(@PathVariable("id") albumId: UUID): ResponseEntity<out ApiResponse<Album>> {
        return albumService.getAlbumById(albumId, AlbumRaw::class)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<Album>(it) },
                        { optionPlanAlbum ->
                            optionPlanAlbum.fold(
                                    { ResponseEntity.notFound().build<ResponseError<Album>>() },
                                    { ResponseEntity.ok(ResponseSuccess(it)) }) }
                )
            }
            .suspendCancellable()
    }
}
