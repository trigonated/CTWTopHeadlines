package com.trigonated.ctwtopheadlines.model.objects

data class PaginatedList<T>(
    val currentPage: Int,
    val totalItemAmount: Int,
    val items: List<T>
) {
    val hasMorePages: Boolean = (totalItemAmount > items.size)

    /** Create a new [PaginatedList] by combining this with the next page. */
    fun addNewPage(page: Int, pageItems: List<T>): PaginatedList<T> {
        return PaginatedList(
            currentPage = page,
            totalItemAmount = totalItemAmount,
            items = items + pageItems
        )
    }
}