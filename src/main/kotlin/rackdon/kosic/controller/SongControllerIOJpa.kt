package rackdon.kosic.controller

import arrow.core.toOption
import arrow.fx.extensions.io.async.effectMap
import arrow.integrations.kotlinx.suspendCancellable
import arrow.syntax.function.partially1
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
import rackdon.kosic.controller.dto.ResponseError
import rackdon.kosic.controller.dto.ResponseSuccess
import rackdon.kosic.controller.dto.SongCreationDto
import rackdon.kosic.controller.dto.SongQueryParams
import rackdon.kosic.controller.exception.ControllerExceptionHandler
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.GroupNotFound
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SortDir
import rackdon.kosic.service.SongServiceIOJpa
import java.net.URI
import java.util.UUID
import javax.validation.Valid

@RestController
@Profile("test", "jpa", "local-jpa")
@RequestMapping("/api/songs")
class SongControllerIOJpa(val songService: SongServiceIOJpa, val controllerExceptionHandler: ControllerExceptionHandler) :
    SongController {

    @PostMapping("")
    override suspend fun createSong(
            @RequestHeader headers: HttpHeaders,
            @RequestBody @Valid songCreationDto: SongCreationDto
    ): ResponseEntity<out ApiResponse<Song>> {
        return songService.createSong(SongCreationDto.toModelCreation(songCreationDto))
            .attempt()
            .effectMap { result ->
                result.fold(
                        { when (it) {
                            is GroupNotFound -> {
                                val apiResponse = ResponseError<Song>(listOf(it.message))
                                ResponseEntity.badRequest().body(apiResponse)
                            }
                            else -> controllerExceptionHandler.handleUnexpectedException(it)
                        } },
                        { ResponseEntity.created(URI("${headers.host}/api/songs/${it.id}")).build() }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("")
    override suspend fun getSongs(@Valid queryParams: SongQueryParams): ResponseEntity<out ApiResponse<DataWithPages<Song>>> {
        val page = queryParams.page?.let { Page(it.toUInt()) }.toOption()
        val pageSize = queryParams.pageSize?.let { PageSize(it.toUInt()) }.toOption()
        val sort = queryParams.sort.toOption()
        val sortDir = queryParams.sortDir?.let { SortDir.valueOf(it) }.toOption()
        val queryFunction = when {
            queryParams.albumId != null -> songService::getSongsByAlbumId.partially1(queryParams.albumId)
            queryParams.albumName != null -> songService::getSongsByAlbumName.partially1(queryParams.albumName)
            queryParams.groupId != null -> songService::getSongsByGroupId.partially1(queryParams.groupId)
            queryParams.groupName != null -> songService::getSongsByGroupName.partially1(queryParams.groupName)
            else -> songService::getSongs
        }
        return queryFunction(SongRaw::class, page, pageSize, sort, sortDir)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<DataWithPages<Song>>(it) },
                        { ResponseEntity.ok(ResponseSuccess(it)) }
                )
            }
            .suspendCancellable()
    }

    @GetMapping("/{id}")
    override suspend fun getSongById(@PathVariable("id") songId: UUID): ResponseEntity<out ApiResponse<Song>> {
        return songService.getSongById(songId, SongRaw::class)
            .attempt()
            .effectMap { result ->
                result.fold(
                        { controllerExceptionHandler.handleUnexpectedException<Song>(it) },
                        { optionPlanSong ->
                            optionPlanSong.fold(
                                    { ResponseEntity.notFound().build<ResponseError<Song>>() },
                                    { ResponseEntity.ok(ResponseSuccess(it)) }) }
                )
            }
            .suspendCancellable()
    }
}
