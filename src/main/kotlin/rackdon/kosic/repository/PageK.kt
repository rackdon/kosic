package rackdon.kosic.repository

import arrow.higherkind
import org.springframework.data.domain.Page

@higherkind
data class PageK<A>(val page: Page<A>) : PageKOf<A>, Page<A> by page

fun <A> Page<A>.k(): PageK<A> = PageK(this)
