package rackdon.kosic.controller

import org.springframework.http.HttpHeaders
import rackdon.kosic.controller.dto.AlbumCreationDto
import rackdon.kosic.controller.dto.AlbumQueryParams
import java.util.UUID

/* This interface returns any because ResponseEntity class or Flux/Mono classes
 doesn't have a constructor without specifying the type
 */
interface AlbumController {
    suspend fun createAlbum(headers: HttpHeaders, albumCreationDto: AlbumCreationDto): Any
    suspend fun getAlbums(queryParams: AlbumQueryParams): Any
    suspend fun getAlbumById(albumId: UUID): Any
}
