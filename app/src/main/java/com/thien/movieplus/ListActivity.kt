package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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

        setSupportActionBar(m_toolbar)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        when (intent.getIntExtra("type", 0)) {
            0 -> finish()
            1 -> fetch("124045", english, goodquality)
            2 -> fetch("124046", english, goodquality)
            3 -> fetch("124047", english, goodquality)
            4 -> fetch("124053", english, goodquality)
            5 -> fetch("134631", english, goodquality)
            6 -> fetch("134629", english, goodquality)
            7 -> fetch("134632", english, goodquality)
            8 -> fetch("134633", english, goodquality)
        }

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        list_list.layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            try {
                val intent = Intent(this, DetailMovieActivity::class.java)
                val movieItem = item as MovieItemSmall
                intent.putExtra("MOVIE_ID", movieItem.movie.id)
                intent.putExtra("MOVIE_POSTER", movieItem.movie.poster_path)
                intent.putExtra("MOVIE_BACKDROP", movieItem.movie.backdrop_path)
                intent.putExtra("MOVIE_TITLE", movieItem.movie.title)
                intent.putExtra("MOVIE_VOTE", movieItem.movie.vote_average)
                intent.putExtra("MOVIE_DATE", movieItem.movie.release_date)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(this, DetailShowActivity::class.java)
                val showItem = item as ShowItemSmall
                intent.putExtra("SHOW_ID", showItem.show.id)
                intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
                intent.putExtra("SHOW_TITLE", showItem.show.name)
                intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
                intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
                startActivity(intent)
            }
        }
    }

    private fun fetch(type: String, english: Boolean, goodquality: Boolean) {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
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
                        adapter.add(MovieItemSmall(m, goodquality))
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

class MyListV4(
    val name: String,
    val results: ArrayList<Movie>
)

class MyListShow(
    val name: String,
    val items: ArrayList<Show>
)

class MyListShowV4(
    val name: String,
    val results: ArrayList<Show>
)