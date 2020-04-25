package rackdon.kosic.service

import arrow.Kind
import arrow.core.Option
import arrow.core.getOrElse
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir

interface PaginationService<T, Y> {
    val defaultPage: Page
    val defaultPageSize: PageSize
    val defaultSort: List<String>
    val defaultSortDir: SortDir

    fun ensurePagination(function: (a: Page, b: PageSize, c: List<String>, d: SortDir) -> Kind<T, Y>,
            page: Option<Page>, pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>
    ): Kind<T, Y> {
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return function(finalPage, finalPageSize, finalSort, finalSortDir)
    }
}
