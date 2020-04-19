package rackdon.kosic.model

data class Page(val value: UInt = 0u)

data class PageSize(val value: UInt = 10u)

data class DataWithPages<T>(
        val data: List<T>,
        val totalPages: UInt
)
