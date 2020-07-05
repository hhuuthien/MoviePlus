package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.search_item.view.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity() {

    private var listSearch = ArrayList<SearchResult>()
    private val adapterSearch = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        supportActionBar?.title = "Tìm kiếm"

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val voiceKey = intent.getStringExtra("voice_key")
        if (voiceKey != null && voiceKey.isNotBlank() && voiceKey.isNotEmpty()) {
            s_key.setText(voiceKey.trim())
            val stringToSearch = s_key.text.toString()
            fetchSearch(stringToSearch, 1, english)
        } else {
            s_key.text = null
        }

        listSearch.clear()
        adapterSearch.clear()

        s_noti.visibility = View.GONE
        s_list.visibility = View.GONE

        s_list.layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)

        adapterSearch.setOnItemClickListener { item, _ ->
            val searchItem = item as SearchItem
            when (searchItem.searchResult.media_type) {
                "movie" -> {
                    val intent = Intent(this, DetailMovieActivity::class.java)
                    intent.putExtra("MOVIE_ID", searchItem.searchResult.id)
                    intent.putExtra("MOVIE_TITLE", searchItem.searchResult.title)
                    intent.putExtra("MOVIE_POSTER", searchItem.searchResult.poster_path)
                    intent.putExtra("MOVIE_BACKDROP", searchItem.searchResult.backdrop_path)
                    intent.putExtra("MOVIE_VOTE", searchItem.searchResult.vote_average)
                    intent.putExtra("MOVIE_DATE", searchItem.searchResult.release_date)
                    startActivity(intent)
                }
                "tv" -> {
                    val intent = Intent(this, DetailShowActivity::class.java)
                    intent.putExtra("SHOW_ID", searchItem.searchResult.id)
                    intent.putExtra("SHOW_TITLE", searchItem.searchResult.name)
                    intent.putExtra("SHOW_POSTER", searchItem.searchResult.poster_path)
                    intent.putExtra("SHOW_BACKDROP", searchItem.searchResult.backdrop_path)
                    intent.putExtra("SHOW_VOTE", searchItem.searchResult.vote_average)
                    startActivity(intent)
                }
                "person" -> {
                    val intent = Intent(this, DetailCastActivity::class.java)
                    intent.putExtra("CAST_ID", searchItem.searchResult.id)
                    intent.putExtra("CAST_NAME", searchItem.searchResult.name)
                    intent.putExtra("CAST_POSTER", searchItem.searchResult.profile_path)
                    startActivity(intent)
                }
            }
        }

        s_key.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotBlank()) {
                    when (s_spinner.selectedIndex) {
                        0 -> fetchSearch(p0.toString().trim(), 1, english)
                        1 -> fetchSearch(p0.toString().trim(), 2, english)
                        2 -> fetchSearch(p0.toString().trim(), 3, english)
                        3 -> fetchSearch(p0.toString().trim(), 4, english)
                    }
                }
            }
        })

        val dataset = LinkedList(listOf("Tất cả", "Movie", "TV show", "Person"))
        s_spinner.attachDataSource(dataset)
        s_spinner.setOnSpinnerItemSelectedListener { _, _, position, _ ->
            if (s_key.text.toString().trim().isNotBlank()) {
                val string = s_key.text.toString().trim()
                when (position) {
                    0 -> fetchSearch(string, 1, english)
                    1 -> fetchSearch(string, 2, english)
                    2 -> fetchSearch(string, 3, english)
                    3 -> fetchSearch(string, 4, english)
                }
            }
        }
    }

    private fun fetchSearch(keyWord: String, mode: Int, english: Boolean) {
        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/search/multi?api_key=d4a7514dbdd976453d2679e036009283&query=$keyWord"
        } else {
            "https://api.themoviedb.org/3/search/multi?api_key=d4a7514dbdd976453d2679e036009283&language=vi&query=$keyWord"
        }

        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ListSearchResult::class.java)

                listSearch = result.results as ArrayList<SearchResult>
            }
        })
        var url2 = ""
        url2 = if (english) {
            "https://api.themoviedb.org/3/search/multi?api_key=d4a7514dbdd976453d2679e036009283&query=$keyWord&page=2"
        } else {
            "https://api.themoviedb.org/3/search/multi?api_key=d4a7514dbdd976453d2679e036009283&language=vi&query=$keyWord&page=2"
        }

        val request2 = Request.Builder()
            .url(url2)
            .build()
        val client2 = OkHttpClient()
        client2.newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ListSearchResult::class.java)

                val listTemp = result.results as ArrayList<SearchResult>
                listSearch.addAll(listTemp)

                runOnUiThread {
                    adapterSearch.clear()

                    val listM = ArrayList<SearchResult>()
                    val listS = ArrayList<SearchResult>()
                    val listP = ArrayList<SearchResult>()

                    for (m in listSearch) {
                        when (m.media_type) {
                            "movie" -> listM.add(m)
                            "tv" -> listS.add(m)
                            "person" -> listP.add(m)
                        }
                    }

                    when (mode) {
                        1 -> {
                            adapterSearch.clear()
                            if (listSearch.size != 0) {
                                s_noti.visibility = View.GONE
                                s_list.visibility = View.VISIBLE

                                for (l in listSearch) {
                                    adapterSearch.add(SearchItem(l))
                                }
                                s_list.adapter = adapterSearch
                            } else {
                                s_noti.visibility = View.VISIBLE
                                s_list.visibility = View.GONE
                            }
                        }
                        2 -> {
                            adapterSearch.clear()
                            if (listM.size != 0) {
                                s_noti.visibility = View.GONE
                                s_list.visibility = View.VISIBLE

                                for (l in listM) {
                                    adapterSearch.add(SearchItem(l))
                                }
                                s_list.adapter = adapterSearch
                            } else {
                                s_noti.visibility = View.VISIBLE
                                s_list.visibility = View.GONE
                            }
                        }
                        3 -> {
                            adapterSearch.clear()
                            if (listS.size != 0) {
                                s_noti.visibility = View.GONE
                                s_list.visibility = View.VISIBLE

                                for (l in listS) {
                                    adapterSearch.add(SearchItem(l))
                                }
                                s_list.adapter = adapterSearch
                            } else {
                                s_noti.visibility = View.VISIBLE
                                s_list.visibility = View.GONE
                            }
                        }
                        4 -> {
                            adapterSearch.clear()
                            if (listP.size != 0) {
                                s_noti.visibility = View.GONE
                                s_list.visibility = View.VISIBLE

                                for (l in listP) {
                                    adapterSearch.add(SearchItem(l))
                                }
                                s_list.adapter = adapterSearch
                            } else {
                                s_noti.visibility = View.VISIBLE
                                s_list.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        })
    }
}

class SearchResult(
    val media_type: String,
    val id: Int,
    val poster_path: String?,
    val profile_path: String?,
    val name: String?,
    val title: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val release_date: String?
)

class ListSearchResult(val results: List<SearchResult>)

class SearchItem(val searchResult: SearchResult) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.search_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            when (searchResult.media_type) {
                "movie" -> {
                    if (searchResult.poster_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_accent)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.poster_path)
                            .fit()
                            .into(viewHolder.itemView.lis_poster)
                    }
                    viewHolder.itemView.lis_title.text = searchResult.title
                }
                "tv" -> {
                    if (searchResult.poster_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_accent)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.poster_path)
                            .fit()
                            .into(viewHolder.itemView.lis_poster)
                    }
                    viewHolder.itemView.lis_title.text = searchResult.name
                }
                "person" -> {
                    if (searchResult.profile_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_accent)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.profile_path)
                            .fit()
                            .into(viewHolder.itemView.lis_poster)
                    }
                    viewHolder.itemView.lis_title.text = searchResult.name
                }
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}