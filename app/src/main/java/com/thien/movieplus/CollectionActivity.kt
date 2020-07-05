package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_collection.*
import okhttp3.*
import java.io.IOException

class CollectionActivity : AppCompatActivity() {

    private var collectionID = -1
    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        col_list.layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            val movieItem = item as MovieItemSmall
            intent.putExtra("MOVIE_ID", movieItem.movie.id)
            intent.putExtra("MOVIE_POSTER", movieItem.movie.poster_path)
            intent.putExtra("MOVIE_BACKDROP", movieItem.movie.backdrop_path)
            intent.putExtra("MOVIE_TITLE", movieItem.movie.title)
            intent.putExtra("MOVIE_VOTE", movieItem.movie.vote_average)
            intent.putExtra("MOVIE_DATE", movieItem.movie.release_date)
            startActivity(intent)
        }

        collectionID = intent.getIntExtra("COLLECTION_ID", -1)

        if (collectionID == -1) {
            finish()
        } else {
            setSupportActionBar(m_toolbar)
            supportActionBar?.title = intent.getStringExtra("COLLECTION_NAME")
            fetch(english, goodquality, collectionID.toString())
        }
    }

    private fun fetch(english: Boolean, goodquality: Boolean, colID: String) {
        col_list.visibility = View.INVISIBLE
        col_progress.visibility = View.VISIBLE

        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/collection/$colID?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/collection/$colID?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, CollectionList::class.java)

                list.clear()
                list = result.parts

                runOnUiThread {
                    adapter.clear()
                    for (m in list) {
                        adapter.add(MovieItemSmall(m, goodquality))
                    }
                    col_list.adapter = adapter

                    col_list.visibility = View.VISIBLE
                    col_progress.visibility = View.INVISIBLE
                }
            }
        })
    }
}

class CollectionList(
    val parts: ArrayList<Movie>
)