package rackdon.kosic.repository.entity.jpa

import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "groups")
data class GroupEntityJpa(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID = UUID.randomUUID(),
        val name: String = "",
        val members: Int = 0,
        val createdOn: LocalDateTime = LocalDateTime.now(),
        val dissolvedOn: LocalDateTime? = null
) {
        companion object {
                fun fromCreation(groupCreation: GroupCreation) =
                        GroupEntityJpa(
                                name = groupCreation.name,
                                members = groupCreation.members.toInt(),
                                createdOn = groupCreation.createdOn,
                                dissolvedOn = groupCreation.dissolvedOn)

                fun toModelRaw(groupEntityJpa: GroupEntityJpa) =
                        GroupRaw(id = groupEntityJpa.id,
                                name = groupEntityJpa.name,
                                members = groupEntityJpa.members.toUInt(),
                                createdOn = groupEntityJpa.createdOn,
                                dissolvedOn = groupEntityJpa.dissolvedOn)
        }
}
