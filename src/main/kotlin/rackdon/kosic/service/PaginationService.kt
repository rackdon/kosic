package rackdon.kosic.service

import arrow.core.Option
import arrow.core.getOrElse
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.SortDir

interface PaginationService {
    val defaultPage: Page
    val defaultPageSize: PageSize
    val defaultSort: List<String>
    val defaultSortDir: SortDir

    fun <T, F : (Page, PageSize, List<String>, SortDir) -> T> F.ensurePagination(page: Option<Page>,
            pageSize: Option<PageSize>, sort: Option<List<String>>, sortDir: Option<SortDir>): T {
        val finalPage = page.getOrElse { defaultPage }
        val finalPageSize = pageSize.getOrElse { defaultPageSize }
        val finalSort = sort.getOrElse { defaultSort }
        val finalSortDir = sortDir.getOrElse { defaultSortDir }
        return this(finalPage, finalPageSize, finalSort, finalSortDir)
    }
}
