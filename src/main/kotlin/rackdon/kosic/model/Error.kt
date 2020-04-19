package rackdon.kosic.model

sealed class Error(override val message: String? = null) : Exception(message)

object GroupNotFound : Error("group does not exist")
object AlbumNotFound : Error("album does not exist")
