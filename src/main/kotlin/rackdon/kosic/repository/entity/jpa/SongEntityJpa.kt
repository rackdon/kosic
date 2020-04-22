package rackdon.kosic.repository.entity.jpa

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import rackdon.kosic.model.SongBase
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SongWithAlbum
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

        fun toModelRaw(songEntityJpa: SongEntityJpa) =
            SongRaw(
                    id = songEntityJpa.id,
                    name = songEntityJpa.name,
                    albumId = songEntityJpa.album.id,
                    duration = songEntityJpa.duration.toUInt(),
                    createdOn = songEntityJpa.createdOn,
                    meta = songEntityJpa.meta)

        fun toModelBase(songEntityJpa: SongEntityJpa) =
            SongBase(
                    name = songEntityJpa.name,
                    duration = songEntityJpa.duration.toUInt(),
                    createdOn = songEntityJpa.createdOn,
                    meta = songEntityJpa.meta)

        fun toModelWithAlbum(songEntityJpa: SongEntityJpa) =
            SongWithAlbum(
                    id = songEntityJpa.id,
                    name = songEntityJpa.name,
                    album = AlbumEntityJpa.toModelRaw(songEntityJpa.album),
                    duration = songEntityJpa.duration.toUInt(),
                    createdOn = songEntityJpa.createdOn,
                    meta = songEntityJpa.meta)
    }
}
