package com.trigonated.ctwtopheadlines.misc

import com.trigonated.ctwtopheadlines.di.NewsApiModule
import com.trigonated.ctwtopheadlines.model.mock.MockNewsApiObjects
import com.trigonated.ctwtopheadlines.model.newsapi.NewsApi
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import retrofit2.Response

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NewsApiModule::class]
)
class MockNewsApiModule {
    @Provides
    fun provideNewsApi(): NewsApi = MockNewsApi()
}

/**
 * Mock version of [NewsApi].
 * Can be configured to fail it's mocked requests.
 */
class MockNewsApi : NewsApi {
    companion object {
        private var topHeadlinesSucceeds: Boolean = true

        /** Sets up stubs for the api calls. **/
        fun setupStubs(
            topHeadlinesSucceeds: Boolean = true
        ) {
            this.topHeadlinesSucceeds = topHeadlinesSucceeds
        }
    }

    override suspend fun topHeadlines(sources: String, page: Int): Response<NewsApiArticleListResponse> {
        return if (topHeadlinesSucceeds) {
            Response.success(MockNewsApiObjects.createMockNewsApiArticleListResponse())
        } else {
            Response.success(MockNewsApiObjects.createMockErrorNewsApiArticleListResponse())
        }
    }
}