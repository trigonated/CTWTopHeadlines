package com.trigonated.ctwtopheadlines.model.mock

import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.model.objects.PaginatedList

/** Contains methods for generating mock data objects. */
object MockObjects {
    /** The last article list generated when calling [createMockPaginatedArticleList()]. */
    var lastArticleList: PaginatedList<Article>? = null
        private set

    /** The last article generated when calling [createMockArticle()]. */
    var lastArticle: Article? = null
        private set

    fun createMockPaginatedArticleList(): PaginatedList<Article> = createMockPaginatedArticleList(null)

    fun createMockPaginatedArticleListFromLastApiMock(): PaginatedList<Article> {
        return createMockPaginatedArticleList(MockNewsApiObjects.lastArticleListResponse)
    }

    private fun createMockPaginatedArticleList(fromApiResult: NewsApiArticleListResponse? = null): PaginatedList<Article> {
        val apiResult = fromApiResult ?: MockNewsApiObjects.createMockNewsApiArticleListResponse()
        return PaginatedList(
            currentPage = 1,
            totalItemAmount = apiResult.totalResults ?: 0,
            items = apiResult.articles!!.map { Article(it) }.sortedByDescending { it.publishedAt }
        ).also { lastArticleList = it }
    }

    fun createMockArticle(): Article {
        lastArticleList?.items?.firstOrNull()?.let { return it }
        val apiItem = MockNewsApiObjects.createMockNewsApiArticleListResponse().articles!!.first()
        return Article(apiItem).also { lastArticle = it }
    }
}