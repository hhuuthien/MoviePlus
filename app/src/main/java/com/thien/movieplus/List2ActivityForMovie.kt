package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_list2.*
import kotlinx.android.synthetic.main.dialog_filter_discover.view.*
import okhttp3.*
import java.io.IOException

class List2ActivityForMovie : AppCompatActivity() {

    private val adapter = GroupAdapter<ViewHolder>()
    private var posYear = 0
    private var totalPages = 0
    private var pageToLoad = 1
    private var year = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list2)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = intent.getStringExtra("NETWORK_NAME")

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        list2_list.layoutManager = layoutManager

        val id = intent.getIntExtra("NETWORK_ID", 0)
        fetchMovie(id.toString(), english, goodquality, year)

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
                //load more
                try {
                    pageToLoad++
                    fetchMovieMore(id.toString(), english, goodquality, year, pageToLoad)
                } catch (e: Exception) {
                    Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_dis_movie, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter -> {
                val inflater = layoutInflater.inflate(R.layout.dialog_filter_network_company, null)
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setView(inflater)
                dialogBuilder.setCancelable(true)
                val dialog = dialogBuilder.create()

                val dataset = ArrayList<String>()
                dataset.add("All years")
                for (year in 2022 downTo 1970) {
                    dataset.add(year.toString())
                }

                val adapter1 = ArrayAdapter(this, R.layout.spinner_item, dataset)
                adapter1.setDropDownViewResource(R.layout.spinner_item_choice)
                val spinnerA = inflater.spinner_a
                spinnerA.adapter = adapter1
                spinnerA.setSelection(posYear, true)

                val buttonApply = inflater.button_apply
                buttonApply.setOnClickListener {
                    dialog.dismiss()

                    val pref = this.getSharedPreferences("SettingPref", 0)
                    val english = pref.getBoolean("english", false)
                    val goodquality = pref.getBoolean("goodquality", true)

                    year = spinnerA.selectedItem.toString()
                    posYear = spinnerA.selectedItemPosition
                    if (year == "All years") {
                        val id = intent.getIntExtra("NETWORK_ID", 0)
                        fetchMovie(id.toString(), english, goodquality, "")
                    } else {
                        val id = intent.getIntExtra("NETWORK_ID", 0)
                        fetchMovie(id.toString(), english, goodquality, year)
                    }
                }

                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchMovie(id: String, english: Boolean, goodquality: Boolean, year: String) {
        list2_noti.visibility = GONE
        list2_list.visibility = INVISIBLE
        list2_progress.visibility = VISIBLE

        val url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&with_companies=$id&primary_release_year=$year&page=1"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&with_companies=$id&language=vi&primary_release_year=$year&page=1"
        }

        pageToLoad = 1

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, DiscoverListMovie::class.java)

                totalPages = result.total_pages

                val list = result.results
                runOnUiThread {
                    adapter.clear()

                    if (list.size == 0) {
                        list2_noti.visibility = VISIBLE
                    } else {
                        list2_noti.visibility = GONE

                        for (m in list) {
                            adapter.add(MovieItemSmall(m, goodquality))
                        }

                        if (pageToLoad < totalPages) {
                            adapter.add(ItemLoadMore(false))
                        }

                        list2_list.adapter = adapter
                    }

                    list2_list.visibility = VISIBLE
                    list2_progress.visibility = INVISIBLE
                }
            }
        })
    }

    private fun fetchMovieMore(
        id: String,
        english: Boolean,
        goodquality: Boolean,
        year: String,
        page: Int
    ) {
        val item = adapter.getItem(adapter.groupCount - 1) as ItemLoadMore
        item.spin = true
        adapter.notifyDataSetChanged()

        val url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&with_companies=$id&primary_release_year=$year&page=${page}"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&with_companies=$id&language=vi&primary_release_year=$year&page=${page}"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, DiscoverListMovie::class.java)

                val listMore = result.results
                runOnUiThread {
                    adapter.removeGroup(adapter.groupCount - 1)
                    for (m in listMore) {
                        adapter.add(MovieItemSmall(m, goodquality))
                    }

                    if (page < totalPages) {
                        adapter.add(ItemLoadMore(false))
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        })
    }
}

class DiscoverListMovie(
    val results: ArrayList<Movie>,
    val total_pages: Int
)