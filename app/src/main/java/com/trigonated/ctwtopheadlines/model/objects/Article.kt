package com.trigonated.ctwtopheadlines.model.objects

import com.trigonated.ctwtopheadlines.misc.StringUtils
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticle
import java.util.*

data class Article(
    val id: String,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val thumbnailUrl: String?,
    val publishedAt: Date?,
    val content: String?,
    val isContentTruncated: Boolean
) {
    constructor(from: NewsApiArticle) : this(
        id = from.url ?: from.hashCode().toString(), // Use the hashcode as ID as rare last resort
        author = from.author,
        title = from.title,
        description = from.description,
        url = from.url,
        thumbnailUrl = from.urlToImage,
        publishedAt = from.publishedAt,
        content = if (from.content != null) StringUtils.cutTruncatedNewsApiString(from.content) else null,
        isContentTruncated = if (from.content != null) StringUtils.isNewsApiStringTruncated(from.content) else false
    )
}