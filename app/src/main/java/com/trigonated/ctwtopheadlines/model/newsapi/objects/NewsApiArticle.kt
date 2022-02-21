package com.trigonated.ctwtopheadlines.model.newsapi.objects

import com.google.gson.annotations.SerializedName
import java.util.*

data class NewsApiArticle(
    @SerializedName("source") val source: NewsApiArticleSource?,
    @SerializedName("author") val author: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: Date?,
    @SerializedName("content") val content: String?,
)

data class NewsApiArticleSource(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
)