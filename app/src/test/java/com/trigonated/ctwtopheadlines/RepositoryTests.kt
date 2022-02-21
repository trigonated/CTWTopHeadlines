package com.trigonated.ctwtopheadlines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.trigonated.ctwtopheadlines.misc.CoroutineDispatcherRule
import com.trigonated.ctwtopheadlines.misc.NewsApiStubs
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.model.newsapi.NewsApi
import com.trigonated.ctwtopheadlines.model.objects.Article
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RepositoryTests {
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Mock(lenient = true)
    private lateinit var newsApi: NewsApi
    private lateinit var repository: Repository

    @Before
    fun setup() = runBlocking {
        repository = Repository(newsApi).apply {
            flowOn = Repository.Dispatcher.MAIN
        }
        NewsApiStubs.setupStubs(newsApi)
    }

    /**
     * Fetch the top headlines (first page).
     * The result should contain a page of top headlines.
     */
    @Test
    fun fetchTopHeadlines() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchTopHeadlines().test {
            assertEquals(Result.Status.LOADING, awaitItem().status)
            val result = awaitItem()
            assertEquals(Result.Status.SUCCESS, result.status)
            assertEquals(MockObjects.createMockPaginatedArticleListFromLastApiMock(), result.data)
            awaitComplete()
        }
    }

    /**
     * Test a failure fetching the top headlines.
     * The result should go into an error state.
     */
    @Test
    fun `fetchTopHeadlines failure`() = runTest(UnconfinedTestDispatcher()) {
        NewsApiStubs.setupStubs(newsApi, topHeadlinesSucceeds = false)
        repository.fetchTopHeadlines().test {
            assertEquals(Result.Status.LOADING, awaitItem().status)
            assertEquals(Result.Status.ERROR, awaitItem().status)
            awaitComplete()
        }
    }

    /**
     * Test fetching more top headlines (next page).
     * The result should contain both fetched pages.
     */
    @Test
    fun fetchMoreTopHeadlines() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchTopHeadlines().test {
            // Load the first page
            assertEquals(Result.Status.LOADING, awaitItem().status)
            val firstPage = awaitItem()
            val firstPageItems: List<Article> = firstPage.data?.items ?: listOf()
            assertEquals(Result.Status.SUCCESS, firstPage.status)
            // Load the second page
            repository.fetchMoreTopHeadlines(firstPage.data).test {
                assertEquals(Result.Status.LOADING, awaitItem().status)
                val secondPage = awaitItem()
                val secondPageItems = MockObjects.createMockPaginatedArticleListFromLastApiMock().items
                val allItems = firstPageItems + secondPageItems
                assertEquals(Result.Status.SUCCESS, secondPage.status)
                assertEquals(2, secondPage.data?.currentPage)
                assertEquals(allItems, secondPage.data?.items)
                assertEquals((allItems.size != secondPage.data?.totalItemAmount), secondPage.data?.hasMorePages)
                awaitComplete()
            }
            awaitComplete()
        }
    }

    /**
     * Test a failure fetching the more top headlines due to not having the first page.
     * The result should go into an error state.
     */
    @Test
    fun `fetchMoreTopHeadlines failure (no currentList)`() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchMoreTopHeadlines(null).test {
            assertEquals(Result.Status.ERROR, awaitItem().status)
            awaitComplete()
        }
    }

    /**
     * Test a failure fetching the more top headlines due to a network error.
     * The result should go into an error state.
     */
    @Test
    fun `fetchMoreTopHeadlines failure (newsApi)`() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchTopHeadlines().test {
            // Load the first page
            assertEquals(Result.Status.LOADING, awaitItem().status)
            val firstPage = awaitItem()
            assertEquals(Result.Status.SUCCESS, firstPage.status)
            // Load the second page
            NewsApiStubs.setupStubs(newsApi, topHeadlinesSucceeds = false)
            repository.fetchMoreTopHeadlines(firstPage.data).test {
                assertEquals(Result.Status.LOADING, awaitItem().status)
                assertEquals(Result.Status.ERROR, awaitItem().status)
                awaitComplete()
            }
            awaitComplete()
        }
    }

    /**
     * Test fetching an article.
     * The result should contain an article.
     */
    @Test
    fun fetchArticle() = runTest(UnconfinedTestDispatcher()) {
        repository.fetchTopHeadlines().test {
            // Load the first page
            assertEquals(Result.Status.LOADING, awaitItem().status)
            val topHeadlines = awaitItem()
            val firstArticle = topHeadlines.data!!.items.first()
            assertEquals(Result.Status.SUCCESS, topHeadlines.status)
            // Load the second page
            repository.fetchArticle(firstArticle.id).test {
                val articleResult = awaitItem()
                assertEquals(Result.Status.SUCCESS, articleResult.status)
                assertEquals(firstArticle, articleResult.data)
                awaitComplete()
            }
            awaitComplete()
        }
    }
}