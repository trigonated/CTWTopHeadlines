package com.trigonated.ctwtopheadlines.model

import com.trigonated.ctwtopheadlines.BuildConfig
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.model.newsapi.NewsApi
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.model.objects.PaginatedList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class Repository @Inject constructor(
    private val newsApi: NewsApi
) {
    /** Where the flows will flow on. Useful for making them flow on [MAIN] during unit tests. */
    var flowOn: Dispatcher = Dispatcher.IO

    /** Very basic in-memory cache. Mostly used to avoid serializing objects between fragments. */
    private val cache = ArticlesCache()

    private suspend fun fetchTopHeadlinesData(page: Int): NewsApiArticleListResponse? {
        return newsApi.topHeadlines(sources = BuildConfig.NEWSAPI_SOURCES, page = page).body()
    }

    /** Fetch the list of top headlines (first page). */
    open fun fetchTopHeadlines(forceRefresh: Boolean = false): Flow<Result<PaginatedList<Article>>> {
        return flow {
            // Try to fetch from cache
            if (!forceRefresh) {
                cache.getTopHeadlines()?.let {
                    emit(Result.success(it))
                    return@flow
                }
            }
            // Loading
            emit(Result.loading())
            // Fetch from API
            val apiCallResult = fetchTopHeadlinesData(page = 1)
            if ((apiCallResult?.isOk == true) && (apiCallResult.articles != null)) {
                // Success
                val result: PaginatedList<Article> = PaginatedList(
                    currentPage = 1,
                    totalItemAmount = apiCallResult.totalResults ?: 0,
                    items = apiCallResult.articles.map { Article(it) }.sortedByDescending { it.publishedAt })
                cache.putTopHeadlines(result)
                emit(Result.success(result))
            } else {
                // Failure
                emit(Result.failure(Error()))
            }
        }.catch { emit(Result.failure(Error())) }.flowOn(flowOn.coroutineDispatcher)
    }

    /** Fetch a new page of top headlines. It returns a concat of the new page with the [currentList]. **/
    open fun fetchMoreTopHeadlines(currentList: PaginatedList<Article>?): Flow<Result<PaginatedList<Article>>> {
        return flow {
            // Fail when no current list
            if (currentList == null) {
                emit(Result.failure(Error()))
                return@flow
            }
            // Finish if there's no more pages
            if (!currentList.hasMorePages) {
                emit(Result.success(currentList))
                return@flow
            }
            // Loading
            emit(Result.loading())
            // Fetch from API
            val apiCallResult = fetchTopHeadlinesData(page = currentList.currentPage + 1)
            if ((apiCallResult?.isOk == true) && (apiCallResult.articles != null)) {
                // Success. Concat the pages
                val result: PaginatedList<Article> = currentList.addNewPage(
                    page = currentList.currentPage + 1,
                    pageItems = apiCallResult.articles.map { Article(it) }.sortedByDescending { it.publishedAt }
                )
                cache.putTopHeadlines(result)
                emit(Result.success(result))
            } else {
                // Failure
                emit(Result.failure(Error()))
            }
        }.catch { emit(Result.failure(Error())) }.flowOn(flowOn.coroutineDispatcher)
    }

    /**
     * Fetch the article with [id].
     *
     * **Warning**: the implementation is very naive and needs the article
     * to be previously cached to a top headlines fetch.
     * */
    open fun fetchArticle(id: String): Flow<Result<Article>> {
        return flow {
            val article: Article? = cache.getArticle(id)

            if (article != null) {
                emit(Result.success(article))
            } else {
                emit(Result.failure(Error()))
            }
        }.catch { emit(Result.failure(Error())) }.flowOn(flowOn.coroutineDispatcher)
    }

    enum class Dispatcher {
        MAIN,
        IO;

        val coroutineDispatcher: CoroutineDispatcher
            get() = when (this) {
                MAIN -> Dispatchers.Main
                IO -> Dispatchers.IO
            }
    }
}

private class ArticlesCache {
    private var cachedTopHeadlines: PaginatedList<Article>? = null

    fun putTopHeadlines(topHeadlines: PaginatedList<Article>) {
        cachedTopHeadlines = topHeadlines
    }

    fun getTopHeadlines(): PaginatedList<Article>? = cachedTopHeadlines

    fun clear() {
        cachedTopHeadlines = null
    }

    fun getArticle(id: String): Article? = cachedTopHeadlines?.items?.find { it.id == id }
}