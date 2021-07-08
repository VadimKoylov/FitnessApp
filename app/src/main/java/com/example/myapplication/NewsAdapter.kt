package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.news_item.view.*

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.PlacesHolder>() {

    var news: MutableList<NewsItems> = mutableListOf()
    private var sourseNewsList: MutableList<NewsItems> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesHolder {
        return PlacesHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.news_item, parent, false)
        )
    }

    private fun filter() {
        news.clear()
        news.addAll(sourseNewsList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(viewHolder: PlacesHolder, position: Int) {
        viewHolder.bind(news[position])
    }

    override fun getItemCount() = news.size

    fun refreshNewsPlaces(news: List<NewsItems>) {
        sourseNewsList.clear()
        sourseNewsList.addAll(news)
        filter()
        notifyDataSetChanged()
    }

    class PlacesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(news: NewsItems) = with(itemView) {
            postBody.text = news.caption
            nameUser.text = news.firstName + " " + news.lastName
            postBody.text = news.caption
        }
    }
}