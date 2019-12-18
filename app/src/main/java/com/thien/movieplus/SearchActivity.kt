package com.thien.movieplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.list_item_search.view.*
import okhttp3.*
import java.io.IOException

class SearchActivity : AppCompatActivity() {

    private var listSearch = ArrayList<SearchResult>()
    private val adapterSearch = GroupAdapter<ViewHolder>()

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        supportActionBar?.title = "Tìm kiếm"

        listSearch.clear()
        adapterSearch.clear()

        s_noti.visibility = View.GONE
        s_list.visibility = View.GONE

        s_key.text = null
        s_key.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

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
                if (p0.toString().trim().isNotEmpty()) {
                    fetchSearch(p0.toString().trim())
                } else {
                    listSearch.clear()
                    adapterSearch.clear()
                }
            }
        })
    }

    private fun fetchSearch(keyWord: String) {
        val url =
            "https://api.themoviedb.org/3/search/multi?api_key=d4a7514dbdd976453d2679e036009283&language=vi&query=$keyWord"
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ListSearchResult::class.java)

                runOnUiThread {
                    listSearch.clear()
                    adapterSearch.clear()

                    listSearch = result.results as ArrayList<SearchResult>

                    if (listSearch.size != 0) {
                        s_noti.visibility = View.GONE
                        s_list.visibility = View.VISIBLE
                    } else {
                        s_noti.visibility = View.VISIBLE
                        s_list.visibility = View.GONE
                    }

                    for (l in listSearch) {
                        adapterSearch.add(SearchItem(l))
                    }
                    s_list.adapter = adapterSearch
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
    val known_for_department: String?,
    val title: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val release_date: String?,
    val overview: String?
)

class ListSearchResult(val results: List<SearchResult>)

class SearchItem(val searchResult: SearchResult) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.list_item_search
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            when (searchResult.media_type) {
                "movie" -> {
                    viewHolder.itemView.lis_type.text = "Phim"
                    if (searchResult.poster_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_blue)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.poster_path)
                            .placeholder(R.drawable.logo_accent)
                            .fit()
                            .into(viewHolder.itemView.lis_poster)
                    }
                    viewHolder.itemView.lis_title.text = searchResult.title
                }
                "tv" -> {
                    viewHolder.itemView.lis_type.text = "TV show"
                    if (searchResult.poster_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_blue)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.poster_path)
                            .placeholder(R.drawable.logo_accent)
                            .fit()
                            .into(viewHolder.itemView.lis_poster)
                    }
                    viewHolder.itemView.lis_title.text = searchResult.name
                }
                "person" -> {
                    viewHolder.itemView.lis_type.text = "Người"
                    if (searchResult.profile_path == null) {
                        viewHolder.itemView.lis_poster.setImageResource(R.drawable.logo_blue)
                    } else {
                        Picasso.get()
                            .load("https://image.tmdb.org/t/p/w300" + searchResult.profile_path)
                            .placeholder(R.drawable.logo_accent)
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
