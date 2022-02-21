package com.trigonated.ctwtopheadlines.model.newsapi.objects

import com.google.gson.annotations.SerializedName

data class NewsApiArticleListResponse(
    @SerializedName("status") val status: Status?,
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("totalResults") val totalResults: Int?,
    @SerializedName("articles") val articles: List<NewsApiArticle>?
) {
    val isOk: Boolean get() = (status == Status.OK)
    val isError: Boolean get() = (status == Status.ERROR)

    enum class Status {
        @SerializedName("ok")
        OK,

        @SerializedName("error")
        ERROR
    }
}