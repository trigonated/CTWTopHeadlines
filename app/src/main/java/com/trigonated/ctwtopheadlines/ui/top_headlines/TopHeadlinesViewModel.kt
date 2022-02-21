package com.trigonated.ctwtopheadlines.ui.top_headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.misc.send
import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.model.objects.PaginatedList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopHeadlinesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val data = MutableStateFlow<Result<PaginatedList<Article>>>(Result.awaitingUserAction())

    val loadingStatus: Flow<Result.Status> = data.map { it.status }

    val items: Flow<List<Article>> = data.mapNotNull { it.data?.items }

    private val _onNonFatalErrorOccurred = Channel<Unit>()
    val onNonFatalErrorOccurred: Flow<Unit> = _onNonFatalErrorOccurred.receiveAsFlow()

    private val _onAuthenticationRequested = Channel<Unit>()
    val onAuthenticationRequested: Flow<Unit> = _onAuthenticationRequested.receiveAsFlow()

    private val _onNavigationToArticleRequested = Channel<String>()
    val onNavigationToArticleRequested: Flow<String> = _onNavigationToArticleRequested.receiveAsFlow()

    init {
        authenticate()
    }

    /** Call at the creation of the viewmodel and when the user retries the authentication. */
    fun authenticate() = viewModelScope.launch {
        _onAuthenticationRequested.send()
    }

    /** Call when a result from the biometric prompt is obtained. */
    fun onAuthenticationResult(success: Boolean) {
        if (success) loadData()
    }

    /** Call when an [item] is clicked. */
    fun onItemClicked(item: Article) = viewModelScope.launch {
        _onNavigationToArticleRequested.send(item.id)
    }

    /** Call when the user requests a refresh. */
    fun refresh() {
        if ((data.value.status != Result.Status.SUCCESS) && (data.value.status != Result.Status.ERROR)) return
        loadData(refreshing = (data.value.status == Result.Status.SUCCESS))
    }

    /** Call when the user reaches the bottom of the list. */
    fun loadMoreItems() {
        if (data.value.status != Result.Status.SUCCESS) return
        if (data.value.data?.hasMorePages != true) return

        viewModelScope.launch {
            repository.fetchMoreTopHeadlines(data.value.data!!).collect {
                data.value = when (it.status) {
                    Result.Status.ERROR ->
                        // Errors are ignored
                        Result.success(data.value.data)
                    Result.Status.LOADING ->
                        // Transform loadings into loading extra data
                        Result.loadingExtraData(data.value.data)
                    else -> it
                }
            }
        }
    }

    private fun loadData(refreshing: Boolean = false) {
        viewModelScope.launch {
            repository.fetchTopHeadlines(forceRefresh = refreshing).collect {
                data.value = if ((it.status == Result.Status.ERROR) && (refreshing)) {
                    // Refresh failed
                    _onNonFatalErrorOccurred.send()
                    Result.success(data.value.data)
                } else if ((it.status == Result.Status.LOADING) && (refreshing)) {
                    // Transform loadings into refreshings
                    Result.refreshing(data.value.data)
                } else {
                    // Success/regular loading/regular error
                    it
                }
            }
        }
    }
}