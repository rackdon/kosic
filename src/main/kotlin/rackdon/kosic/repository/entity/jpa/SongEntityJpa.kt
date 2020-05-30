package rackdon.kosic.repository.entity.jpa

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import rackdon.kosic.model.SongBase
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SongWithAlbum
import rackdon.kosic.model.SongWithAlbumAndGroup
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
@Table(name = "songs")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class SongEntityJpa(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: UUID = UUID.randomUUID(),
        val name: String = "",
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        val album: AlbumEntityJpa = AlbumEntityJpa(),
        val duration: Int = 0,
        val createdOn: LocalDateTime = LocalDateTime.now(),
        @Type(type = "jsonb")
        val meta: Map<String, Any> = emptyMap()
) {
    fun toModelRaw() =
        SongRaw(
            id = this.id,
            name = this.name,
            albumId = this.album.id,
            duration = this.duration.toUInt(),
            createdOn = this.createdOn,
            meta = this.meta)

    fun toModelBase() =
        SongBase(
            name = this.name,
            duration = this.duration.toUInt(),
            createdOn = this.createdOn,
            meta = this.meta)

    fun toModelWithAlbum() =
        SongWithAlbum(
            id = this.id,
            name = this.name,
            album = this.album.toModelRaw(),
            duration = this.duration.toUInt(),
            createdOn = this.createdOn,
            meta = this.meta)

    fun toModelWithAlbumAndGroup() =
        SongWithAlbumAndGroup(
            id = this.id,
            name = this.name,
            album = this.album.toModelRaw(),
            group = this.album.group.toModelRaw(),
            duration = this.duration.toUInt(),
            createdOn = this.createdOn,
            meta = this.meta)

    companion object {
        fun fromCreation(songCreation: SongCreation, albumEntityJpa: AlbumEntityJpa): SongEntityJpa {
            require(songCreation.albumId == albumEntityJpa.id) {
                "Album id from song creation model (${songCreation.albumId}) " +
                        "must be the same as album entity id (${albumEntityJpa.id})"
            }
            return SongEntityJpa(
                    name = songCreation.name,
                    album = albumEntityJpa,
                    duration = songCreation.duration.toInt(),
                    createdOn = songCreation.createdOn,
                    meta = songCreation.meta)
        }
    }
}
