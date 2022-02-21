package com.trigonated.ctwtopheadlines.misc

import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.mock.MockObjects
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.model.objects.PaginatedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.mockito.AdditionalAnswers
import org.mockito.Mockito

/** Contains methods to set up stubs for mocking a [Repository]. **/
object RepositoryStubs {
    /** Sets up stubs for the repository methods. **/
    fun setupStubs(
        repository: Repository,
        fetchTopHeadlinesSucceeds: Boolean = true,
        fetchTopHeadlinesRefreshSucceeds: Boolean = true,
        fetchMoreTopHeadlinesSucceeds: Boolean = true,
        fetchArticleSucceeds: Boolean = true
    ) {
        // fetchTopHeadlines
        Mockito.`when`(repository.fetchTopHeadlines(Mockito.anyBoolean()))
            .thenAnswer(AdditionalAnswers.answer<Flow<Result<PaginatedList<Article>>>, Boolean> { forceRefresh ->
                return@answer flow {
                    emit(Result.loading())
                    if ((!forceRefresh) && (fetchTopHeadlinesSucceeds)) {
                        emit(Result.success(MockObjects.createMockPaginatedArticleList()))
                    } else if ((forceRefresh) && (fetchTopHeadlinesRefreshSucceeds)) {
                        emit(Result.success(MockObjects.createMockPaginatedArticleList()))
                    } else {
                        emit(Result.failure(Error()))
                    }
                }.flowOn(Dispatchers.Main)
            })
        // fetchMoreTopHeadlines
        Mockito.`when`(repository.fetchMoreTopHeadlines(Mockito.any()))
            .thenAnswer(AdditionalAnswers.answer<Flow<Result<PaginatedList<Article>>>, PaginatedList<Article>>
            { currentList ->
                return@answer flow {
                    emit(Result.loading())
                    if (fetchMoreTopHeadlinesSucceeds) {
                        emit(
                            Result.success(
                                currentList.addNewPage(
                                    page = currentList.currentPage + 1,
                                    pageItems = MockObjects.createMockPaginatedArticleList().items
                                )
                            )
                        )
                    } else {
                        emit(Result.failure(Error()))
                    }

                }.flowOn(Dispatchers.Main)
            })
        // fetchArticle
        Mockito.`when`(repository.fetchArticle(Mockito.anyString()))
            .thenAnswer(AdditionalAnswers.answer<Flow<Result<Article>>, String> {
                return@answer flow {
                    emit(Result.loading())
                    if (fetchArticleSucceeds) {
                        emit(Result.success(MockObjects.createMockArticle()))
                    } else {
                        emit(Result.failure(Error()))
                    }
                }.flowOn(Dispatchers.Main)
            })
    }
}