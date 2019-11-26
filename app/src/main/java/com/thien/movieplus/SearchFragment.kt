package com.thien.movieplus

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.list_item_search.view.*
import okhttp3.*
import java.io.IOException

class SearchFragment : Fragment() {

    private var listSearch = ArrayList<SearchResult>()
    private val adapterSearch = GroupAdapter<ViewHolder>()

    override fun onPause() {
        super.onPause()
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        listSearch.clear()
        adapterSearch.clear()

        view.findViewById<TextView>(R.id.s_noti).visibility = GONE
        view.findViewById<RecyclerView>(R.id.s_list).visibility = GONE

        view.findViewById<EditText>(R.id.s_key).text = null

        view.findViewById<EditText>(R.id.s_key).requestFocus()
        val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, HIDE_IMPLICIT_ONLY)

        view.findViewById<EditText>(R.id.s_key).addTextChangedListener(object : TextWatcher {
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

        adapterSearch.setOnItemClickListener { item, _ ->
            val searchItem = item as SearchItem
            when (searchItem.searchResult.media_type) {
                "movie" -> {
                    val intent = Intent(context, DetailMovieActivity::class.java)
                    intent.putExtra("MOVIE_ID", searchItem.searchResult.id)
                    intent.putExtra("MOVIE_TITLE", searchItem.searchResult.title)
                    intent.putExtra("MOVIE_POSTER", searchItem.searchResult.poster_path)
                    intent.putExtra("MOVIE_BACKDROP", searchItem.searchResult.backdrop_path)
                    intent.putExtra("MOVIE_VOTE", searchItem.searchResult.vote_average)
                    intent.putExtra("MOVIE_DATE", searchItem.searchResult.release_date)
                    startActivity(intent)
                }
                "tv" -> {
                    val intent = Intent(context, DetailShowActivity::class.java)
                    intent.putExtra("SHOW_ID", searchItem.searchResult.id)
                    intent.putExtra("SHOW_TITLE", searchItem.searchResult.name)
                    intent.putExtra("SHOW_POSTER", searchItem.searchResult.poster_path)
                    intent.putExtra("SHOW_BACKDROP", searchItem.searchResult.backdrop_path)
                    intent.putExtra("SHOW_VOTE", searchItem.searchResult.vote_average)
                    startActivity(intent)
                }
                "person" -> {
                    val intent = Intent(context, DetailCastActivity::class.java)
                    intent.putExtra("CAST_ID", searchItem.searchResult.id)
                    intent.putExtra("CAST_NAME", searchItem.searchResult.name)
                    intent.putExtra("CAST_POSTER", searchItem.searchResult.profile_path)
                    startActivity(intent)
                }
            }
        }
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

                activity?.runOnUiThread {
                    listSearch.clear()
                    adapterSearch.clear()

                    listSearch = result.results as ArrayList<SearchResult>

                    if (listSearch.size != 0) {
                        s_noti.visibility = GONE
                        s_list.visibility = VISIBLE
                    } else {
                        s_noti.visibility = VISIBLE
                        s_list.visibility = GONE
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
