package rackdon.kosic.repository.entity.jpa

import rackdon.kosic.model.AlbumBase
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.AlbumWithGroup
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "albums")
data class AlbumEntityJpa(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID = UUID.randomUUID(),
        val name: String = "",
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        val group: GroupEntityJpa = GroupEntityJpa(),
        val createdOn: LocalDateTime = LocalDateTime.now()
) {
        companion object {
                fun fromCreation(albumCreation: AlbumCreation, groupEntityJpa: GroupEntityJpa): AlbumEntityJpa {
                        require(albumCreation.groupId == groupEntityJpa.id) {
                                "Group id from album creation model (${albumCreation.groupId}) " +
                                        "must be the same as group entity id (${groupEntityJpa.id})"
                        }
                        return AlbumEntityJpa(
                                name = albumCreation.name,
                                group = groupEntityJpa,
                                createdOn = albumCreation.createdOn)
                }

                fun toModelRaw(albumEntityJpa: AlbumEntityJpa) =
                        AlbumRaw(
                                id = albumEntityJpa.id,
                                name = albumEntityJpa.name,
                                groupId = albumEntityJpa.group.id,
                                createdOn = albumEntityJpa.createdOn)

                fun toModelBase(albumEntityJpa: AlbumEntityJpa) =
                        AlbumBase(
                                name = albumEntityJpa.name,
                                createdOn = albumEntityJpa.createdOn)

                fun toModelWithGroup(albumEntityJpa: AlbumEntityJpa) =
                        AlbumWithGroup(
                                id = albumEntityJpa.id,
                                name = albumEntityJpa.name,
                                group = GroupEntityJpa.toModelRaw(albumEntityJpa.group),
                                createdOn = albumEntityJpa.createdOn)
        }
}
