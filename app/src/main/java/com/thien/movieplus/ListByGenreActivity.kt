package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_list_by_genre.*
import okhttp3.*
import java.io.IOException

class ListByGenreActivity : AppCompatActivity() {

    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_by_genre)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        lbg_list.layoutManager = layoutManager

        val listYear = ArrayList<Int>()
        for (y in 2019 downTo 2010) {
            listYear.add(y)
        }
        val adapterSpinner = ArrayAdapter(this, R.layout.spinner_item, listYear)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                fetchMovieByGenre(spinner.selectedItem.toString())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        spinner.adapter = adapterSpinner

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

    private fun fetchMovieByGenre(year: String) {
        lbg_list.visibility = View.INVISIBLE
        lbg_progress.visibility = View.VISIBLE

        val genreId = intent.getIntExtra("genre_id", -1)
        if (genreId != -1) {
            val url =
                "https://api.themoviedb.org/3/discover/movie?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&year=${year}&with_genres=${genreId}"
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        lbg_progress.visibility = View.INVISIBLE
                        Snackbar
                            .make(lbg_layout, "Không có kết nối", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val gSon = GsonBuilder().create()
                    val detailList = gSon.fromJson(body, MyList2::class.java)

                    list.clear()
                    list = detailList.results

                    runOnUiThread {
                        supportActionBar?.title = intent.getStringExtra("genre_name")

                        adapter.clear()
                        for (m in list) {
                            adapter.add(MovieItemRow(m))
                        }
                        lbg_list.adapter = adapter

                        lbg_list.visibility = View.VISIBLE
                        lbg_progress.visibility = View.INVISIBLE
                    }
                }
            })
        }
    }
}

class MyList2(
    val results: ArrayList<Movie>
)
