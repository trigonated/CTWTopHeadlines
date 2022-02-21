package com.trigonated.ctwtopheadlines.model.mock

import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticle
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleSource
import java.time.Instant
import java.util.*

/** Contains methods for generating mock NewsApi objects. */
object MockNewsApiObjects {
    private const val TOTAL_RESULTS: Int = 20
    private const val RESULTS_PER_PAGE: Int = 10

    /** The last article list response generated when calling [createMockNewsApiArticleListResponse()]. */
    var lastArticleListResponse: NewsApiArticleListResponse? = null
        private set

    fun createMockNewsApiArticleListResponse(): NewsApiArticleListResponse {
        val source: NewsApiArticleSource = createMockNewsApiArticleSource(id = "mocknews")
        return NewsApiArticleListResponse(
            status = NewsApiArticleListResponse.Status.OK,
            code = null,
            message = null,
            totalResults = TOTAL_RESULTS,
            articles = buildList {
                for (i in 0 until RESULTS_PER_PAGE) {
                    add(createMockArticle(index = i, source = source))
                }
            }
        ).also { lastArticleListResponse = it }
    }

    private fun createMockNewsApiArticleSource(id: String) = NewsApiArticleSource(
        id = id,
        name = id.uppercase()
    )

    private fun createMockArticle(index: Int, source: NewsApiArticleSource) = NewsApiArticle(
        source = source,
        author = "Author $index",
        title = "Article $index",
        description = "Description of article $index",
        url = "http://example.com/${source.id}/article$index",
        urlToImage = null,
        publishedAt = Date.from(Instant.now().minusSeconds(60L * index)),
        content = "Content of article $index"
    )

    fun createMockErrorNewsApiArticleListResponse(): NewsApiArticleListResponse {
        return NewsApiArticleListResponse(
            status = NewsApiArticleListResponse.Status.ERROR,
            code = "unexpectedError",
            message = null,
            totalResults = null,
            articles = null
        )
    }
}