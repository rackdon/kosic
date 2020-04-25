package rackdon.kosic.controller

import org.springframework.http.HttpHeaders
import rackdon.kosic.controller.dto.GroupCreationDto
import rackdon.kosic.controller.dto.GroupQueryParams
import java.util.UUID

/* This interface returns any because ResponseEntity class or Flux/Mono classes
 doesn't have a constructor without specifying the type
 */
interface GroupController {
    suspend fun createGroup(headers: HttpHeaders, groupCreationDto: GroupCreationDto): Any
    suspend fun getGroups(queryParams: GroupQueryParams): Any
    suspend fun getGroupById(groupId: UUID): Any
}
