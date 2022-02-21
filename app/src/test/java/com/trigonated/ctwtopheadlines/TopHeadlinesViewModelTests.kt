package com.trigonated.ctwtopheadlines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.trigonated.ctwtopheadlines.misc.CoroutineDispatcherRule
import com.trigonated.ctwtopheadlines.misc.RepositoryStubs
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.misc.expectMostRecentItem
import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.ui.top_headlines.TopHeadlinesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class TopHeadlinesViewModelTests {
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Mock(lenient = true)
    private lateinit var repository: Repository
    private lateinit var viewModel: TopHeadlinesViewModel

    @Before
    fun setup() {
        viewModel = TopHeadlinesViewModel(repository)
        RepositoryStubs.setupStubs(repository)
    }

    /**
     * Test not doing anything (as if the user just entered the screen).
     * The viewmodel should be in a state where it's waiting for the user to authenticate themselves.
     */
    @Test
    fun `initial state is waiting for authentication`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test the authentication failing.
     * The viewmodel should continue waiting for the user authentication.
     */
    @Test
    fun `continue waiting for authentication if authentication fails`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(false)
            expectNoEvents()
        }
    }

    /**
     * Test the user authenticating themselves successfully and loading the first page of data.
     * The viewmodel should contain a list of articles.
     */
    @Test
    fun `load data (first page) after authentication`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            assertEquals(MockObjects.lastArticleList?.items, viewModel.items.expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test a failure loading the data.
     * The viewmodel should go into an error state.
     */
    @Test
    fun `load data (first page) failure`() = runTest(UnconfinedTestDispatcher()) {
        RepositoryStubs.setupStubs(repository, fetchTopHeadlinesSucceeds = false)
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.ERROR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test refreshing the data (after loading).
     * The viewmodel should go to a success state after briefly passing by a refreshing state.
     */
    @Test
    fun refresh() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            viewModel.refresh()
            assertEquals(Result.Status.REFRESHING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test a failure refreshing the data.
     * The viewmodel should notify that an error occurred and then return to a success state.
     */
    @Test
    fun `refresh failure`() = runTest(UnconfinedTestDispatcher()) {
        RepositoryStubs.setupStubs(repository, fetchTopHeadlinesRefreshSucceeds = false)
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            viewModel.refresh()
            assertEquals(Result.Status.REFRESHING, awaitItem())
            assertEquals(Unit, viewModel.onNonFatalErrorOccurred.expectMostRecentItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test loading more data (second page).
     * The viewmodel should contain both pages.
     */
    @Test
    fun `load more data (second page)`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            val firstPageItems: List<Article> = MockObjects.lastArticleList?.items ?: listOf()
            viewModel.loadMoreItems()
            assertEquals(Result.Status.LOADING_EXTRA_DATA, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            val secondPageItems: List<Article> = MockObjects.lastArticleList?.items ?: listOf()
            assertEquals(firstPageItems + secondPageItems, viewModel.items.expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test simulating clicking on an item.
     * The viewmodel should request navigation to the clicked article.
     */
    @Test
    fun `navigate when item clicked`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            assertEquals(Result.Status.AWAITING_USER_ACTION, awaitItem())
            viewModel.onAuthenticationResult(true)
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            val firstItem: Article = viewModel.items.expectMostRecentItem().first()
            viewModel.onItemClicked(firstItem)
            assertEquals(firstItem.id, viewModel.onNavigationToArticleRequested.expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}