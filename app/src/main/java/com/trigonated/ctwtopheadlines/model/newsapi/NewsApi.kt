package com.trigonated.ctwtopheadlines.model.newsapi

import com.google.gson.GsonBuilder
import com.trigonated.ctwtopheadlines.BuildConfig
import com.trigonated.ctwtopheadlines.model.newsapi.objects.NewsApiArticleListResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines")
    suspend fun topHeadlines(
        @Query("sources") sources: String,
        @Query("page") page: Int
    ): Response<NewsApiArticleListResponse>

    companion object {
        fun create(): NewsApi {
            val client = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val requestBuilder: Request.Builder = chain.request().newBuilder()
                    requestBuilder.header("X-Api-Key", BuildConfig.NEWSAPI_API_KEY)
                    return@addNetworkInterceptor chain.proceed(requestBuilder.build())
                }
                .build()
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.NEWSAPI_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(NewsApi::class.java)
        }
    }
}