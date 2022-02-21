package com.trigonated.ctwtopheadlines.misc

import com.trigonated.ctwtopheadlines.model.mock.MockNewsApiObjects
import com.trigonated.ctwtopheadlines.model.newsapi.NewsApi
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import retrofit2.Response

/** Contains methods to set up stubs for mocking a [NewsApi]. **/
object NewsApiStubs {
    /** Sets up stubs for the api calls. **/
    suspend fun setupStubs(
        newsApi: NewsApi,
        topHeadlinesSucceeds: Boolean = true
    ) {
        Mockito.`when`(newsApi.topHeadlines(Mockito.anyString(), Mockito.anyInt()))
            .thenAnswer(AdditionalAnswers.answer<Response<NewsApiArticleListResponse>, String, Int> { _, _ ->
                return@answer if (topHeadlinesSucceeds) {
                    Response.success(MockNewsApiObjects.createMockNewsApiArticleListResponse())
                } else {
                    Response.success(MockNewsApiObjects.createMockErrorNewsApiArticleListResponse())
                }
            })

    }
}