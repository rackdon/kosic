package rackdon.kosic.repository

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import rackdon.kosic.model.Page
import rackdon.kosic.model.PageSize
import rackdon.kosic.model.Pagination
import rackdon.kosic.model.SortDir
import reactor.core.publisher.Flux

object Pagination {

    fun <F, T : Flux<F>> T.paginated(page: Page, pageSize: PageSize): Flux<F> {
        return this.skip((page.value * pageSize.value).toLong())
            .take(pageSize.value.toLong())
    }

    fun getSort(sort: List<String>, sortDir: SortDir): Sort {
        val direction = Sort.Direction.valueOf(sortDir.name)
        return if (sort.isEmpty()) Sort.unsorted() else Sort.by(direction, *sort.toTypedArray())
    }

    fun getPageRequest(pagination: Pagination): PageRequest {
        val finalSort = getSort(pagination.sort, pagination.sortDir)
        return PageRequest.of(pagination.page.value.toInt(), pagination.pageSize.value.toInt(), finalSort)
    }
}
