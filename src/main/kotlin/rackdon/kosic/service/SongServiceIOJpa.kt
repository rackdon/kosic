package rackdon.kosic.service

import arrow.core.Option
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.syntax.function.partially1
import org.springframework.stereotype.Service
import rackdon.kosic.model.DataWithPages
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Song
import rackdon.kosic.model.SongCreation
import rackdon.kosic.model.SongRaw
import rackdon.kosic.model.SortDir
import rackdon.kosic.repository.SongRepositoryIOJpa
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.streams.toList
import org.springframework.data.domain.Page as JpaPage

@Service
class SongServiceIOJpa(private val songRepository: SongRepositoryIOJpa) : SongService<ForIO, JpaPage<out Song>> {
    override val defaultPage = Page()
    override val defaultPageSize = PageSize()
    override val defaultSort = emptyList<String>()
    override val defaultSortDir = SortDir.DESC

    override fun createSong(songCreation: SongCreation): IO<SongRaw> {
        return songRepository.save(songCreation)
    }

    override fun getSongs(projection: KClass<out Song>, page: Option<Page>, pageSize: Option<PageSize>,
            sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val songs = !super.ensurePagination(songRepository::findAll.partially1(projection), page, pageSize, sort, sortDir)
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
            val songs = !super.ensurePagination(songRepository::findByAlbumId.partially1(albumId)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByAlbumName(albumName: String, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val songs = !super.ensurePagination(songRepository::findByAlbumName.partially1(albumName)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByGroupId(groupId: UUID, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val songs = !super.ensurePagination(songRepository::findByGroupId.partially1(groupId)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }

    override fun getSongsByGroupName(groupName: String, projection: KClass<out Song>, page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): IO<DataWithPages<Song>> {
        return IO.fx {
            val songs = !super.ensurePagination(songRepository::findByGroupName.partially1(groupName)
                .partially1(projection), page, pageSize, sort, sortDir)
            DataWithPages(songs.get().toList(), songs.totalPages.toUInt())
        }
    }
}
