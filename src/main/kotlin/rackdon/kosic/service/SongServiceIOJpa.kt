package rackdon.kosic.service

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import org.springframework.stereotype.Service
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.SongRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList

@Service
class SongServiceIOJpa(private val songRepository: SongRepositoryIOJpa) : SongService<ForIO> {

    override fun createSong(songCreation: SongCreation): IO<SongRaw> {
        return songRepository.save(songCreation)
    }

    override fun getSongs(projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val songs = !songRepository.findAll(projection, pagination)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongById(id: UUID, projection: KClass<out Song>): IO<Option<Song>> {
        return songRepository.findById(id, projection)
    }

    override fun getSongByName(name: String, projection: KClass<out Song>): IO<Option<Song>> {
        return songRepository.findByName(name, projection)
    }

    override fun getSongsByAlbumId(albumId: UUID, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val songs = !songRepository.findByAlbumId(albumId, projection, pagination)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByAlbumName(albumName: String, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val songs = !songRepository.findByAlbumName(albumName, projection, pagination)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByGroupId(groupId: UUID, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val songs = !songRepository.findByGroupId(groupId, projection, pagination)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByGroupName(groupName: String, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val pagination = Pagination().getPagination(page, pageSize, sort, sortDir)
            val songs = !songRepository.findByGroupName(groupName, projection, pagination)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }
}
