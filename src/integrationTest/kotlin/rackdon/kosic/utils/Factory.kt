package rackdon.kosic.utils

import rackdon.kosic.model.AlbumCreation
import rackdon.kosic.model.AlbumRaw
import rackdon.kosic.model.GroupCreation
import rackdon.kosic.model.GroupRaw
import rackdon.kosic.model.SongCreation

interface Factory {
    fun insertGroup(groupCreation: GroupCreation? = null): Any
    fun insertAlbum(albumCreation: AlbumCreation? = null, group: GroupRaw? = null): Any
    fun insertSong(songCreation: SongCreation? = null, album: AlbumRaw? = null): Any
}
