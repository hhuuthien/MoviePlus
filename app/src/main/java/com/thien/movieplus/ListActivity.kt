package com.thien.movieplus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_list.*
import okhttp3.*
import java.io.IOException


class ListActivity : AppCompatActivity() {

    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        when (intent.getIntExtra("type", 0)) {
            0 -> finish()
            1 -> fetch("124045")
            2 -> fetch("124046")
            3 -> fetch("124047")
            4 -> fetch("124053")
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_list.layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            val movieItem = item as MovieItemRow
            intent.putExtra("MOVIE_ID", movieItem.movie.id)
            intent.putExtra("MOVIE_POSTER", movieItem.movie.poster_path)
            intent.putExtra("MOVIE_BACKDROP", movieItem.movie.backdrop_path)
            intent.putExtra("MOVIE_TITLE", movieItem.movie.title)
            intent.putExtra("MOVIE_VOTE", movieItem.movie.vote_average)
            intent.putExtra("MOVIE_DATE", movieItem.movie.release_date)
            startActivity(intent)
        }
    }

    private fun fetch(type: String) {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        val url =
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    list_progress.visibility = INVISIBLE
                    Snackbar
                        .make(list_layout, "Không có kết nối", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Thử lại") {
                            fetch(type)
                        }
                        .setActionTextColor(Color.WHITE)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailList = gSon.fromJson(body, MyList::class.java)

                list.clear()
                list = detailList.items

                runOnUiThread {
                    supportActionBar?.title = detailList.name

                    adapter.clear()
                    for (m in list) {
                        adapter.add(MovieItemRow(m))
                    }
                    list_list.adapter = adapter

                    list_list.visibility = VISIBLE
                    list_progress.visibility = INVISIBLE
                }
            }
        })
    }
}

class MyList(
    val name: String,
    val items: ArrayList<Movie>
)
