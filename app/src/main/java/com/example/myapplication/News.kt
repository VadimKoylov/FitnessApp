package com.example.myapplication

import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class News : AppCompatActivity() {
    private val scope = MainScope()

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    fun asyncShowNewsData(token: String, userViewModel: NewsModel) = scope.launch {
        showNewsData(token, userViewModel)
    }

    private suspend fun showNewsData(token: String, userViewModel: NewsModel) {
        progress_bar.visibility = View.VISIBLE
        news_list.visibility = View.GONE

        withContext(Dispatchers.IO) {
            val caption: String = ""
            val firstName: String = ""
            val lastName: String = ""

            val messages: MutableList<NewsItems> = mutableListOf<NewsItems>()

            try {
                val reqParam =
                    URLEncoder.encode("limit", "UTF-8") + "=" + URLEncoder.encode(
                        "10",
                        "UTF-8"
                    ) +
                            "&" + URLEncoder.encode(
                        "offset",
                        "UTF-8"
                    ) + "=" + URLEncoder.encode("0", "UTF-8") +
                            "&" + URLEncoder.encode(
                        "maxDate",
                        "UTF-8"
                    ) + "=" + URLEncoder.encode("2021-05-06T18:26:42.820994", "UTF-8")

                val url = "https://app.ferfit.club/api/feed?"
                val endpoint = URL(url + reqParam)

                val myConnection1: HttpsURLConnection =
                    endpoint.openConnection() as HttpsURLConnection

                val authorizationKey = token
                val contentType = "application/json"
                myConnection1.setRequestProperty("Authorization", "Bearer $authorizationKey")
                myConnection1.setRequestProperty("Content-Type", contentType)
                myConnection1.requestMethod = "GET";

                val responseBody: InputStream = myConnection1.inputStream
                val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
                val jsonReader = JsonReader(responseBodyReader)

                jsonReader.beginObject()

                while (jsonReader.hasNext()) {
                    val key = jsonReader.nextName()
                    if (key == "result") {

                        jsonReader.beginObject()
                        while (jsonReader.hasNext()) {
                            val arrayKey = jsonReader.nextName()
                            if (arrayKey == "posts") {

                                jsonReader.beginArray()
                                while (jsonReader.hasNext()) {

                                    fun readMessage(reader: JsonReader): NewsItems {
                                        val news = NewsItems(caption, firstName, lastName)
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            val name = reader.nextName()
                                            if (name == "caption") {
                                                if (reader.peek() != JsonToken.NULL) {
                                                    news.caption = reader.nextString()
                                                } else {
                                                    news.caption = reader.nextNull().toString()
                                                }
                                            } else if (name == "user") {
                                                jsonReader.beginObject()
                                                while (jsonReader.hasNext()) {
                                                    when (reader.nextName()) {
                                                        "firstName" -> {
                                                            news.firstName = reader.nextString()
                                                        }
                                                        "lastName" -> {
                                                            news.lastName = reader.nextString()
                                                        }
                                                        else -> {
                                                            reader.skipValue()
                                                        }
                                                    }
                                                }
                                                jsonReader.endObject()
                                            } else {
                                                reader.skipValue()
                                            }
                                        }
                                        reader.endObject()
                                        return news
                                    }

                                    var buff = readMessage(jsonReader)
                                    messages.add(buff)
                                    userViewModel.addNews(buff)
                                }
                                break;
                            } else {
                                jsonReader.skipValue()
                            }
                        }
                        break;
                    } else {
                        jsonReader.skipValue()
                    }
                }
                jsonReader.close();

                myConnection1.disconnect();
                Log.println(Log.ERROR, "News", "NAPAS")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        progress_bar.visibility = View.GONE
        news_list.visibility = View.VISIBLE
        Log.println(Log.ERROR, "News", "LAVANDOS")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val token = intent.getStringExtra("token")

        val userViewModel by lazy { ViewModelProviders.of(this).get(NewsModel::class.java) }
        val adapter = NewsAdapter()

        news_list.layoutManager = LinearLayoutManager(this)
        news_list.adapter = adapter

        userViewModel.getListNews().observe(this, Observer {
            it?.let {
                adapter.refreshNewsPlaces(it)
                adapter.notifyDataSetChanged()
            }
        })

        if (token != null) {
            asyncShowNewsData(token, userViewModel)
        }


        val navButton: BottomNavigationView = this.findViewById(R.id.NavigationButton)

        navButton.visibility = View.VISIBLE
        layout_news.visibility = View.VISIBLE
        layout_food.visibility = View.INVISIBLE
        layout_user.visibility = View.INVISIBLE
        layout_training.visibility = View.INVISIBLE
        layout_map.visibility = View.INVISIBLE
        navButton.menu.findItem(R.id.navigation_news).isEnabled = false
        navButton.menu.findItem(R.id.navigation_food).isEnabled = true
        navButton.menu.findItem(R.id.navigation_user).isEnabled = true
        navButton.menu.findItem(R.id.navigation_training).isEnabled = true
        navButton.menu.findItem(R.id.navigation_map).isEnabled = true

        fun updateUI() {
            when (intent.getIntExtra(NEWS_PAGE_TAG, NEWS_PAGE)) {
                NEWS_PAGE -> {
                    navButton.visibility = View.VISIBLE
                    layout_news.visibility = View.VISIBLE
                    layout_food.visibility = View.INVISIBLE
                    layout_user.visibility = View.INVISIBLE
                    layout_training.visibility = View.INVISIBLE
                    layout_map.visibility = View.INVISIBLE
                    navButton.menu.findItem(R.id.navigation_news).isEnabled = false
                    navButton.menu.findItem(R.id.navigation_food).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_user).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_training).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_map).isEnabled = true
                }
                FOOD_PAGE -> {
                    navButton.visibility = View.VISIBLE
                    layout_news.visibility = View.INVISIBLE
                    layout_food.visibility = View.VISIBLE
                    layout_user.visibility = View.INVISIBLE
                    layout_training.visibility = View.INVISIBLE
                    layout_map.visibility = View.INVISIBLE
                    navButton.menu.findItem(R.id.navigation_news).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_food).isEnabled = false
                    navButton.menu.findItem(R.id.navigation_user).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_training).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_map).isEnabled = true
                }
                USER_PAGE -> {
                    navButton.visibility = View.VISIBLE
                    layout_news.visibility = View.INVISIBLE
                    layout_food.visibility = View.INVISIBLE
                    layout_user.visibility = View.VISIBLE
                    layout_training.visibility = View.INVISIBLE
                    layout_map.visibility = View.INVISIBLE
                    navButton.menu.findItem(R.id.navigation_news).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_food).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_user).isEnabled = false
                    navButton.menu.findItem(R.id.navigation_training).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_map).isEnabled = true
                }
                TRAINING_PAGE -> {
                    navButton.visibility = View.VISIBLE
                    layout_news.visibility = View.INVISIBLE
                    layout_food.visibility = View.INVISIBLE
                    layout_user.visibility = View.INVISIBLE
                    layout_training.visibility = View.VISIBLE
                    layout_map.visibility = View.INVISIBLE
                    navButton.menu.findItem(R.id.navigation_news).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_food).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_user).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_training).isEnabled = false
                    navButton.menu.findItem(R.id.navigation_map).isEnabled = true
                }
                MAP_PAGE -> {
                    navButton.visibility = View.VISIBLE
                    layout_news.visibility = View.INVISIBLE
                    layout_food.visibility = View.INVISIBLE
                    layout_user.visibility = View.INVISIBLE
                    layout_training.visibility = View.INVISIBLE
                    layout_map.visibility = View.VISIBLE
                    navButton.menu.findItem(R.id.navigation_news).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_food).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_user).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_training).isEnabled = true
                    navButton.menu.findItem(R.id.navigation_map).isEnabled = false
                }
            }
        }

        navButton.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_news -> {
                    intent.putExtra(NEWS_PAGE_TAG, NEWS_PAGE)
                    updateUI()
                }
                R.id.navigation_food -> {
                    intent.putExtra(NEWS_PAGE_TAG, FOOD_PAGE)
                    updateUI()
                }
                R.id.navigation_user -> {
                    intent.putExtra(NEWS_PAGE_TAG, USER_PAGE)
                    updateUI()
                }
                R.id.navigation_training -> {
                    intent.putExtra(NEWS_PAGE_TAG, TRAINING_PAGE)
                    updateUI()
                }
                R.id.navigation_map -> {
                    intent.putExtra(NEWS_PAGE_TAG, MAP_PAGE)
                    updateUI()
                }
            }
            true
        }

    }
    companion object {
        const val NEWS_PAGE_TAG = "news_page"
        const val NEWS_PAGE = 1
        const val FOOD_PAGE = 2
        const val USER_PAGE = 3
        const val TRAINING_PAGE = 4
        const val MAP_PAGE = 5
    }
}
