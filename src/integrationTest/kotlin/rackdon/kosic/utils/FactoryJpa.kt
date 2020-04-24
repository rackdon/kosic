package rackdon.kosic.utils
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SongCreation
import rackdon.kosic.repository.entity.jpa.AlbumEntityJpa
import rackdon.kosic.repository.entity.jpa.GroupEntityJpa
import rackdon.kosic.repository.entity.jpa.SongEntityJpa
import rackdon.kosic.utils.generator.albumCreation
import rackdon.kosic.utils.generator.groupCreation
import rackdon.kosic.utils.generator.songCreation
import javax.persistence.EntityManager
import kotlin.reflect.full.memberProperties

class FactoryJpa(private val entityManager: EntityManager) : Factory {

    private inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    private fun insertFromMap(tableName: String, data: Map<String, Any?>) {
        val columns = data.keys.joinToString(", ")
        val values = data.values.map { x -> when (x) {
            is String -> "'$x'"
            is Map<*, *> -> "'$x'"
            else -> x
        } }.joinToString(", ")
        val query = "INSERT INTO $tableName($columns) VALUES($values)"
        entityManager.createNativeQuery(query).executeUpdate()
    }

    private fun <T : Any> insert(entity: T): T {
        return entityManager.merge(entity)
    }

    private fun <T : Any> find(entity: Class<T>, entityId: Any): T? {
        return entityManager.find(entity, entityId)
    }

    override fun insertGroup(groupCreation: GroupCreation?): GroupEntityJpa {
        val groupEntityJpa = GroupEntityJpa.fromCreation(groupCreation ?: Arb.groupCreation().single())
        return insert(groupEntityJpa)
    }

    override fun insertAlbum(albumCreation: AlbumCreation?, group: GroupRaw?): AlbumEntityJpa {
        val groupEntityJpa = when (group) {
            null -> insertGroup()
            else -> find(GroupEntityJpa::class.java, group.id)
                ?: insertGroup(GroupCreation(group.name, group.members, group.createdOn, group.dissolvedOn))
        }
        val finalAlbumCreation = albumCreation ?: Arb.albumCreation().single()
        val albumEntityJpa = AlbumEntityJpa(
                name = finalAlbumCreation.name,
                group = groupEntityJpa,
                createdOn = finalAlbumCreation.createdOn)
        return insert(albumEntityJpa)
    }

    override fun insertSong(songCreation: SongCreation?, album: AlbumRaw?): SongEntityJpa {
        val albumJpa = when (album) {
            null -> insertAlbum()
            else -> find(AlbumEntityJpa::class.java, album.id) ?: insertAlbum(AlbumCreation(album.name, album.groupId, album.createdOn))
        }
        val finalSongCreation = songCreation ?: Arb.songCreation().single()
        val songEntityJpa = SongEntityJpa(
                name = finalSongCreation.name,
                album = albumJpa,
                duration = finalSongCreation.duration.toInt(),
                createdOn = finalSongCreation.createdOn,
                meta = finalSongCreation.meta)
        return insert(songEntityJpa)
    }
}
