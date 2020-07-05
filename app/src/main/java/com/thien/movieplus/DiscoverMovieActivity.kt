package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_discover_movie.*
import kotlinx.android.synthetic.main.dialog_filter_discover.view.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DiscoverMovieActivity : AppCompatActivity() {

    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()
    private var posYear = 2
    private var posGenre = 0
    private var year = "2020"
    private var genre = 28
    private var pageToLoad = 1
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_movie)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = "Discover Movie"

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
                    val pref = this.getSharedPreferences("SettingPref", 0)
                    val english = pref.getBoolean("english", false)
                    val goodquality = pref.getBoolean("goodquality", true)
                    pageToLoad++
                    fetchMovieByGenreLoadMore(year, genre, english, goodquality, pageToLoad)
                } catch (e: Exception) {
                    Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_LONG).show()
                }
            }
        }

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        discover_list.layoutManager = layoutManager

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        genre = intent.getIntExtra("genre_id", 28)
        when (genre) {
            28 -> {
                posGenre = 0
            }
            53 -> {
                posGenre = 1
            }
            9648 -> {
                posGenre = 2
            }
            14 -> {
                posGenre = 3
            }
            36 -> {
                posGenre = 4
            }
            12 -> {
                posGenre = 5
            }
            80 -> {
                posGenre = 6
            }
            16 -> {
                posGenre = 7
            }
            878 -> {
                posGenre = 8
            }
            35 -> {
                posGenre = 9
            }
            10749 -> {
                posGenre = 10
            }
            27 -> {
                posGenre = 11
            }
            18 -> {
                posGenre = 12
            }
            10751 -> {
                posGenre = 13
            }
            10402 -> {
                posGenre = 14
            }
            10752 -> {
                posGenre = 15
            }
            -1 -> {

            }
        }
        fetchMovieByGenre(year, genre, english, goodquality)
    }

    private fun fetchMovieByGenre(
        year: String,
        genre: Int,
        english: Boolean,
        goodquality: Boolean
    ) {
        discover_list.visibility = View.INVISIBLE
        discover_progress.visibility = VISIBLE
        discover_noti.visibility = GONE

        pageToLoad = 1

        val url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, MyList2::class.java)

                totalPages = result.total_pages

                list.clear()
                list = result.results
                runOnUiThread {
                    adapter.clear()
                    discover_list.visibility = VISIBLE
                    discover_progress.visibility = GONE

                    if (list.size == 0) {
                        discover_noti.visibility = VISIBLE
                    } else {
                        discover_noti.visibility = GONE

                        for (m in list) {
                            adapter.add(MovieItemSmall(m, goodquality))
                        }

                        if (pageToLoad < totalPages) {
                            adapter.add(ItemLoadMore(false))
                        }

                        discover_list.adapter = adapter
                    }
                }
            }
        })
    }

    private fun fetchMovieByGenreLoadMore(
        year: String,
        genre: Int,
        english: Boolean,
        goodquality: Boolean,
        pageToLoad: Int
    ) {
        val item = adapter.getItem(adapter.groupCount - 1) as ItemLoadMore
        item.spin = true
        adapter.notifyDataSetChanged()

        val url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=${pageToLoad}"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=${pageToLoad}"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, MyList2::class.java)

                val listMore = result.results
                runOnUiThread {
                    adapter.removeGroup(adapter.groupCount - 1)
                    for (m in listMore) {
                        adapter.add(MovieItemSmall(m, goodquality))
                    }

                    if (pageToLoad < totalPages) {
                        adapter.add(ItemLoadMore(false))
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_dis_movie, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_filter -> {
                val inflater = layoutInflater.inflate(R.layout.dialog_filter_discover, null)
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setView(inflater)
                dialogBuilder.setCancelable(true)
                val dialog = dialogBuilder.create()

                val dataset1 = LinkedList(
                    listOf(
                        "Action",//0
                        "Thriller",//1
                        "Mystery",//2
                        "Fantasy",//3
                        "History",//4
                        "Advanture",//5
                        "Crime",//6
                        "Animation",//7
                        "Sci-Fi",//8
                        "Comedy",//9
                        "Romance",//10
                        "Horror",//1
                        "Drama",//12
                        "Family",//13
                        "Musical",//14
                        "War"//15
                    )
                )

                val adapter1 = ArrayAdapter(this, R.layout.spinner_item, dataset1)
                adapter1.setDropDownViewResource(R.layout.spinner_item_choice)
                val spinnerA = inflater.spinner_a
                spinnerA.adapter = adapter1
                spinnerA.setSelection(posGenre, true)

                val dataset2 = ArrayList<String>()
                for (year in 2022 downTo 1900) {
                    dataset2.add(year.toString())
                }

                val adapter2 = ArrayAdapter(this, R.layout.spinner_item, dataset2)
                adapter2.setDropDownViewResource(R.layout.spinner_item_choice)
                val spinnerB = inflater.spinner_b
                spinnerB.adapter = adapter2
                spinnerB.setSelection(posYear, true)

                val buttonApply = inflater.button_apply
                buttonApply.setOnClickListener {
                    dialog.dismiss()

                    year = spinnerB.selectedItem.toString()
                    when (spinnerA.selectedItemPosition) {
                        0 -> genre = 28
                        1 -> genre = 53
                        2 -> genre = 9648
                        3 -> genre = 14
                        4 -> genre = 36
                        5 -> genre = 12
                        6 -> genre = 80
                        7 -> genre = 16
                        8 -> genre = 878
                        9 -> genre = 35
                        10 -> genre = 10749
                        11 -> genre = 27
                        12 -> genre = 18
                        13 -> genre = 10751
                        14 -> genre = 10402
                        15 -> genre = 10752
                    }

                    posGenre = spinnerA.selectedItemPosition
                    posYear = spinnerB.selectedItemPosition

                    val pref = this.getSharedPreferences("SettingPref", 0)
                    val english = pref.getBoolean("english", false)
                    val goodquality = pref.getBoolean("goodquality", true)

                    fetchMovieByGenre(year, genre, english, goodquality)
                }

                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class MyList2(
    val results: ArrayList<Movie>,
    val total_pages: Int
)