package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_discover_show.*
import kotlinx.android.synthetic.main.dialog_filter_discover.view.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DiscoverShowActivity : AppCompatActivity() {

    private var list = ArrayList<Show>()
    private val adapter = GroupAdapter<ViewHolder>()
    private var posYear = 2
    private var posGenre = 0
    private var year = "2020"
    private var genre = 10759
    private var pageToLoad = 1
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_show)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = "Discover TV show"

        adapter.setOnItemClickListener { item, _ ->
            try {
                val intent = Intent(this, DetailShowActivity::class.java)
                val showItem = item as ShowItemSmall
                intent.putExtra("SHOW_ID", showItem.show.id)
                intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
                intent.putExtra("SHOW_TITLE", showItem.show.name)
                intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
                intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
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

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        discover_list.layoutManager = layoutManager

        fetchMovieByGenre(year, genre, english, goodquality)
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
                        "Action & Adventure",//0
                        "Animation",//1
                        "Comedy",//2
                        "Crime",//3
                        "Documentary",//4
                        "Drama",//5
                        "Family",//6
                        "Mystery",//7
                        "Sci-Fi & Fantasy"//8
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
                        0 -> genre = 10759
                        1 -> genre = 16
                        2 -> genre = 35
                        3 -> genre = 80
                        4 -> genre = 99
                        5 -> genre = 18
                        6 -> genre = 10751
                        7 -> genre = 9648
                        8 -> genre = 10765
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

    private fun fetchMovieByGenre(
        year: String,
        genre: Int,
        english: Boolean,
        goodquality: Boolean
    ) {
        discover_list.visibility = View.INVISIBLE
        discover_progress.visibility = View.VISIBLE
        discover_noti.visibility = View.GONE

        pageToLoad = 1

        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/discover/tv?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&first_air_date_year=${year}&with_genres=${genre}"
        } else {
            "https://api.themoviedb.org/3/discover/tv?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&first_air_date_year=${year}&with_genres=${genre}"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, MyList3::class.java)

                totalPages = result.total_pages

                list.clear()
                list = result.results
                runOnUiThread {
                    adapter.clear()
                    discover_list.visibility = View.VISIBLE
                    discover_progress.visibility = View.GONE

                    if (list.size == 0) {
                        discover_noti.visibility = View.VISIBLE
                    } else {
                        discover_noti.visibility = View.GONE

                        for (m in list) {
                            adapter.add(ShowItemSmall(m, goodquality))
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
        val itemLoadMore = adapter.getItem(adapter.groupCount - 1) as ItemLoadMore
        itemLoadMore.spin = true
        adapter.notifyDataSetChanged()

        val url = if (english) {
            "https://api.themoviedb.org/3/discover/tv?api_key=d4a7514dbdd976453d2679e036009283&sort_by=popularity.desc&first_air_date_year=${year}&with_genres=${genre}&page=${pageToLoad}"
        } else {
            "https://api.themoviedb.org/3/discover/tv?api_key=d4a7514dbdd976453d2679e036009283&language=vi&sort_by=popularity.desc&first_air_date_year=${year}&with_genres=${genre}&page=${pageToLoad}"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, MyList3::class.java)

                val listMore = result.results
                runOnUiThread {
                    adapter.removeGroup(adapter.groupCount - 1)
                    for (m in listMore) {
                        adapter.add(ShowItemSmall(m, goodquality))
                    }

                    if (pageToLoad < totalPages) {
                        adapter.add(ItemLoadMore(false))
                    }

                    adapter.notifyDataSetChanged()
                }
            }
        })
    }
}

class MyList3(
    val results: ArrayList<Show>,
    val total_pages: Int
)