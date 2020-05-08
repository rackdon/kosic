package rackdon.kosic.model

import arrow.core.Option
import arrow.core.getOrElse

data class Page(val value: UInt = 0u)

data class PageSize(val value: UInt = 10u)

enum class SortDir { ASC, DESC }

data class Pagination(
        val page: Page = Page(),
        val pageSize: PageSize = PageSize(),
        val sort: List<String> = emptyList(),
        val sortDir: SortDir = SortDir.DESC
) {
    fun getPagination(page: Option<Page>, pageSize: Option<PageSize>, sort: Option<List<String>>,
            sortDir: Option<SortDir>
    ): Pagination {
        val finalPage = page.getOrElse { this.page }
        val finalPageSize = pageSize.getOrElse { this.pageSize }
        val finalSort = sort.getOrElse { this.sort }
        val finalSortDir = sortDir.getOrElse { this.sortDir }
        return Pagination(finalPage, finalPageSize, finalSort, finalSortDir)
    }
}

data class DataWithPages<T>(
        val data: List<T>,
        val totalPages: UInt
)
