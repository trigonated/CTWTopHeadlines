package com.trigonated.ctwtopheadlines.misc

object StringUtils {
    private val newsApiTruncatedRegex = Regex("\\[\\+\\d*\\schars]\$")

    /** Cuts the `[+1234 chars]` part at the end of truncated NewsApi strings. */
    fun cutTruncatedNewsApiString(str: String): String {
        return str.replace(newsApiTruncatedRegex, "")
    }

    /** Whether a NewsApi string is truncated or not. */
    fun isNewsApiStringTruncated(str: String): Boolean = str.contains(newsApiTruncatedRegex)
}