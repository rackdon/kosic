package rackdon.kosic.utils

import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SongCreation
import rackdon.kosic.repository.entity.mongo.GroupEntityMongo
import rackdon.kosic.utils.generator.groupCreation

class FactoryMongo(private val reactiveMongoTemplate: ReactiveMongoTemplate) : Factory {
    override fun insertGroup(groupCreation: GroupCreation?): GroupEntityMongo {
        val groupEntityMongo = GroupEntityMongo.fromCreation(groupCreation ?: Arb.groupCreation().single())
        return reactiveMongoTemplate.save(groupEntityMongo).block()!!
    }

    override fun insertAlbum(albumCreation: AlbumCreation?, group: GroupRaw?): Any {
        TODO("Not yet implemented")
    }

    override fun insertSong(songCreation: SongCreation?, album: AlbumRaw?): Any {
        TODO("Not yet implemented")
    }
}
