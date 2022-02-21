package com.trigonated.ctwtopheadlines.di

import com.trigonated.ctwtopheadlines.model.newsapi.NewsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NewsApiModule {
    @Singleton
    @Provides
    fun provideNewsApi(): NewsApi = NewsApi.create()
}