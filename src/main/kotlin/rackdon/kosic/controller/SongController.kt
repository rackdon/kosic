package rackdon.kosic.controller

import org.springframework.http.HttpHeaders
import rackdon.kosic.controller.dto.SongCreationDto
import rackdon.kosic.controller.dto.SongQueryParams
import java.util.UUID

/* This interface returns any because ResponseEntity class or Flux/Mono classes
 doesn't have a constructor without specifying the type
 */
interface SongController {
    suspend fun createSong(headers: HttpHeaders, songCreationDto: SongCreationDto): Any
    suspend fun getSongs(queryParams: SongQueryParams): Any
    suspend fun getSongById(songId: UUID): Any
}
