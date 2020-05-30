package rackdon.kosic.repository.entity.mongo

import org.springframework.data.mongodb.core.mapping.Document
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Document(collection = "groups")
data class GroupEntityMongo(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID = UUID.randomUUID(),
        val name: String = "",
        val members: Int = 0,
        val createdOn: LocalDateTime = LocalDateTime.now(),
        val dissolvedOn: LocalDateTime? = null
) {
    fun toModelRaw() =
        GroupRaw(
            id = this.id,
            name = this.name,
            members = this.members.toUInt(),
            createdOn = this.createdOn,
            dissolvedOn = this.dissolvedOn
        )
    companion object {
        fun fromCreation(groupCreation: GroupCreation) =
            GroupEntityMongo(
                    name = groupCreation.name,
                    members = groupCreation.members.toInt(),
                    createdOn = groupCreation.createdOn,
                    dissolvedOn = groupCreation.dissolvedOn
            )
    }
}
