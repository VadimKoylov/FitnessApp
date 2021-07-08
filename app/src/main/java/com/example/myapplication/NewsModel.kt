package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewsModel: ViewModel() {
    private val newsList = MutableLiveData<MutableList<NewsItems>>()

    init {
        newsList.value = ArrayList()
    }

    fun getListNews() = newsList

    fun addNews(news: NewsItems) {
        newsList.value?.add(news)
        newsList.postValue(newsList.value)
    }
}