package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_discover.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DiscoverActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        supportActionBar?.title = "Khám phá"

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

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        discover_list.layoutManager = layoutManager

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

        val adapterSpinner1 = ArrayAdapter(this, R.layout.spinner_item, dataset1)
        adapterSpinner1.setDropDownViewResource(R.layout.spinner_item_choice)
        dis_spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                var genre = -1
                when (i) {
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
                val year = dis_spinner2.selectedItem.toString()
                fetchMovieByGenre(year, genre, english, goodquality)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        dis_spinner1.adapter = adapterSpinner1

        when (intent.getIntExtra("genre_id", -1)) {
            28 -> dis_spinner1.setSelection(0, true)
            53 -> dis_spinner1.setSelection(1, true)
            9648 -> dis_spinner1.setSelection(2, true)
            14 -> dis_spinner1.setSelection(3, true)
            36 -> dis_spinner1.setSelection(4, true)
            12 -> dis_spinner1.setSelection(5, true)
            80 -> dis_spinner1.setSelection(6, true)
            16 -> dis_spinner1.setSelection(7, true)
            878 -> dis_spinner1.setSelection(8, true)
            35 -> dis_spinner1.setSelection(9, true)
            10749 -> dis_spinner1.setSelection(10, true)
            27 -> dis_spinner1.setSelection(11, true)
            18 -> dis_spinner1.setSelection(12, true)
            10751 -> dis_spinner1.setSelection(13, true)
            10402 -> dis_spinner1.setSelection(14, true)
            10752 -> dis_spinner1.setSelection(15, true)
            -1 -> dis_spinner1.setSelection(0, true)
        }

        val dataset2 = ArrayList<String>()
        for (year in 2022 downTo 1900) {
            dataset2.add(year.toString())
        }

        val adapterSpinner2 = ArrayAdapter(this, R.layout.spinner_item, dataset2)
        adapterSpinner2.setDropDownViewResource(R.layout.spinner_item_choice)
        dis_spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val year = dis_spinner2.selectedItem.toString()
                var genre = -1
                when (dis_spinner1.selectedItemPosition) {
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
                fetchMovieByGenre(year, genre, english, goodquality)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        dis_spinner2.adapter = adapterSpinner2
        dis_spinner2.setSelection(2, true)
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

        var url = ""
        url = if (english) {
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

                val list = result.results
                fetchMovieByGenre2(year, genre, english, goodquality, list)
            }
        })
    }

    private fun fetchMovieByGenre2(
        year: String,
        genre: Int,
        english: Boolean,
        goodquality: Boolean,
        listBefore: ArrayList<Movie>
    ) {
        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=2"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=2"
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

                val list = result.results
                listBefore.addAll(list)

                fetchMovieByGenre3(year, genre, english, goodquality, listBefore)
            }
        })
    }

    private fun fetchMovieByGenre3(
        year: String,
        genre: Int,
        english: Boolean,
        goodquality: Boolean,
        listBefore: ArrayList<Movie>
    ) {
        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=3"
        } else {
            "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&primary_release_year=${year}&with_genres=${genre}&page=3"
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

                val list = result.results
                listBefore.addAll(list)

                runOnUiThread {
                    adapter.clear()
                    discover_list.visibility = VISIBLE
                    discover_progress.visibility = GONE

                    if (listBefore.size == 0) {
                        discover_noti.visibility = VISIBLE
                    } else {
                        discover_noti.visibility = GONE

                        for (m in listBefore) {
                            adapter.add(MovieItemSmall(m, goodquality))
                        }
                        discover_list.adapter = adapter
                    }
                }
            }
        })
    }
}

class MyList2(
    val results: ArrayList<Movie>
)