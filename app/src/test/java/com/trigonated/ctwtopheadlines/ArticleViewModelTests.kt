package com.trigonated.ctwtopheadlines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.trigonated.ctwtopheadlines.misc.CoroutineDispatcherRule
import com.trigonated.ctwtopheadlines.misc.RepositoryStubs
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.misc.expectMostRecentItem
import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.ui.article.ArticleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ArticleViewModelTests {
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Mock(lenient = true)
    private lateinit var repository: Repository
    private lateinit var viewModel: ArticleViewModel

    @Before
    fun setup() {
        val savedStateHandle = SavedStateHandle(mapOf("articleId" to "someid"))
        viewModel = ArticleViewModel(savedStateHandle, repository)
        RepositoryStubs.setupStubs(repository)
    }

    /**
     * Test not receiving the required articleId argument.
     * The viewmodel should go into an error state.
     */
    @Test
    fun `missing arguments`() = runTest(UnconfinedTestDispatcher()) {
        val savedStateHandle = SavedStateHandle(mapOf())
        viewModel = ArticleViewModel(savedStateHandle, repository)

        viewModel.loadingStatus.test {
            assertEquals(Result.Status.LOADING, awaitItem())
            viewModel.onCreated()
            assertEquals(Result.Status.ERROR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test loading the data successfully.
     * The viewmodel should go into a success state and have an article.
     */
    @Test
    fun `load data`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            viewModel.onCreated()
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            assertEquals(MockObjects.lastArticle, viewModel.article.expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test a failure loading the data.
     * The viewmodel should go into an error state.
     */
    @Test
    fun `load data failure`() = runTest(UnconfinedTestDispatcher()) {
        RepositoryStubs.setupStubs(repository, fetchArticleSucceeds = false)
        viewModel.loadingStatus.test {
            viewModel.onCreated()
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.ERROR, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Test simulating the user requesting that the article is opened in the browser.
     * The viewmodel should request navigation to an url (opening the browser).
     */
    @Test
    fun `open article url when open in browser is requested`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.loadingStatus.test {
            viewModel.onCreated()
            assertEquals(Result.Status.LOADING, awaitItem())
            assertEquals(Result.Status.SUCCESS, awaitItem())
            val articleUrl: String? = viewModel.article.expectMostRecentItem().url
            assertNotEquals(null, articleUrl)
            viewModel.openInBrowser()
            assertEquals(articleUrl, viewModel.onNavigationToUrlRequested.expectMostRecentItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}