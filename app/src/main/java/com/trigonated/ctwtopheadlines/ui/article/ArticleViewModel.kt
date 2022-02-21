package com.trigonated.ctwtopheadlines.ui.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.model.Repository
import com.trigonated.ctwtopheadlines.model.objects.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: Repository
) : ViewModel() {
    private val articleIdArgument: String? = savedStateHandle["articleId"]
    private var onCreatedCalled: Boolean = false

    private val data = MutableStateFlow<Result<Article>>(Result.loading())

    val loadingStatus: Flow<Result.Status> = data.map { it.status }

    val article: Flow<Article> = data.mapNotNull { it.data }

    private val _onNavigationToUrlRequested = Channel<String>()
    val onNavigationToUrlRequested: Flow<String> = _onNavigationToUrlRequested.receiveAsFlow()

    /** Call when the view is created. Required because of the unit tests. */
    fun onCreated() {
        if (onCreatedCalled) return
        onCreatedCalled = true
        loadData()
    }

    /** Call when the user clicks on a "view in browser" button. */
    fun openInBrowser() {
        val url: String = data.value.data?.url ?: return
        viewModelScope.launch {
            _onNavigationToUrlRequested.send(url)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            if (articleIdArgument != null) {
                // Load the data
                repository.fetchArticle(articleIdArgument).collect {
                    data.value = it
                }
            } else {
                // Something navigated to this screen without passing the argument
                data.value = Result.failure(null)
            }
        }
    }
}