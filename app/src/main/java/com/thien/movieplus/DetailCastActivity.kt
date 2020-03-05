package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_detail_cast.*
import kotlinx.android.synthetic.main.product_of_cast_item.view.*
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime

class DetailCastActivity : AppCompatActivity() {

    var castID: Int = -1
    private var posterPath: String = ""

    private var listProduct = ArrayList<ProductOfCast>()
    private var listMovie = ArrayList<ProductOfCast>()
    private var listShow = ArrayList<ProductOfCast>()
    private val adapterMovie = GroupAdapter<ViewHolder>()
    private val adapterShow = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_cast)

        dc_deathday_text.visibility = View.GONE
        dc_deathday.visibility = View.GONE

        castID = intent.getIntExtra("CAST_ID", -1)
        if (castID == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Name and Title
            val castPoster = intent.getStringExtra("CAST_POSTER")
            if (castPoster == null || castPoster.isEmpty()) {
                dc_poster.setImageResource(R.drawable.logo_accent)
            } else {
                posterPath = castPoster
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300$castPoster")
                    .fit()
                    .into(dc_poster)
            }

            val castName = intent.getStringExtra("CAST_NAME")
            if (castName != null && castName.isNotEmpty()) {
                dc_name.text = castName
            }

            fetch(castID.toString())
            fetchProduct(castID.toString(), castName!!)
        }

        dc_name.setOnClickListener {
            Toast.makeText(this, "ID: $castID", Toast.LENGTH_LONG).show()
        }

        dc_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dc_movie.layoutManager = layoutManager
        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dc_tvshow.layoutManager = layoutManager2

        adapterShow.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailShowActivity::class.java)
            val myItem = item as ProductOfCastItem
            intent.putExtra("SHOW_ID", myItem.productOfCast.id)
            intent.putExtra("SHOW_TITLE", myItem.productOfCast.name)
            intent.putExtra("SHOW_POSTER", myItem.productOfCast.poster_path)
            intent.putExtra("SHOW_BACKDROP", myItem.productOfCast.backdrop_path)
            intent.putExtra("SHOW_VOTE", myItem.productOfCast.vote_average)
            startActivity(intent)
        }

        adapterMovie.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailMovieActivity::class.java)
            val myItem = item as ProductOfCastItem
            intent.putExtra("MOVIE_ID", myItem.productOfCast.id)
            intent.putExtra("MOVIE_TITLE", myItem.productOfCast.title)
            intent.putExtra("MOVIE_POSTER", myItem.productOfCast.poster_path)
            intent.putExtra("MOVIE_DATE", myItem.productOfCast.release_date)
            intent.putExtra("MOVIE_VOTE", myItem.productOfCast.vote_average)
            intent.putExtra("MOVIE_BACKDROP", myItem.productOfCast.backdrop_path)
            startActivity(intent)
        }
    }

    private fun fetch(castId: String) {
        val url =
            "https://api.themoviedb.org/3/person/$castId?api_key=d4a7514dbdd976453d2679e036009283"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailCast = gSon.fromJson(body, DetailCast::class.java)

                val isDead = detailCast.deathday != null

                runOnUiThread {
                    if (detailCast.birthday == null) {
                        dc_birthday.text = "Đang cập nhật"
                    } else {
                        val day = detailCast.birthday.substring(8, 10)
                        val month = detailCast.birthday.substring(5, 7)
                        val year = detailCast.birthday.substring(0, 4)
                        dc_birthday.text = "$day-$month-$year"

                        if (!isDead) {
                            val currentYear = LocalDateTime.now().year
                            val age = currentYear - year.toInt()
                            dc_birthday.append("  ($age tuổi)")
                        }
                    }

                    if (detailCast.place_of_birth == null) {
                        dc_place.text = "Đang cập nhật"
                    } else {
                        dc_place.text = detailCast.place_of_birth
                    }

                    if (isDead) {
                        dc_deathday_text.visibility = View.VISIBLE
                        dc_deathday.visibility = View.VISIBLE

                        val day = detailCast.deathday?.substring(8, 10)
                        val month = detailCast.deathday?.substring(5, 7)
                        val year = detailCast.deathday?.substring(0, 4)
                        dc_deathday.text = "$day-$month-$year"
                    } else {
                        dc_deathday_text.visibility = View.GONE
                        dc_deathday.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun fetchProduct(castId: String, castName: String) {
        val url =
            "https://api.themoviedb.org/3/person/$castId/combined_credits?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultProductOfCast::class.java)

                listProduct.clear()
                listProduct = result.cast

                listMovie.clear()
                listShow.clear()

                for (m in listProduct) {
                    if (m.media_type == "movie") {
                        listMovie.add(m)
                    } else if (m.media_type == "tv") {
                        listShow.add(m)
                    }
                }

                runOnUiThread {
                    if (listMovie.size == 0) {
                        dc_movie_text.text = "$castName chưa tham gia phim nào"
                    } else {
                        listMovie.sortByDescending { it.popularity }
                    }

                    if (listShow.size == 0) {
                        dc_tvshow_text.text = "$castName chưa tham gia TV show nào"
                    }

                    adapterMovie.clear()
                    adapterShow.clear()

                    try {
                        for (m in listMovie) {
                            adapterMovie.add(
                                ProductOfCastItem(m)
                            )
                        }
                        for (m in listShow) {
                            adapterShow.add(
                                ProductOfCastItem(m)
                            )
                        }
                        dc_movie.adapter = adapterMovie
                        dc_tvshow.adapter = adapterShow
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }
                }
            }
        })
    }
}

class DetailCast(
    val birthday: String?,
    val deathday: String?,
    val id: Int,
    val name: String,
    val place_of_birth: String?
)

class ResultProductOfCast(
    val cast: ArrayList<ProductOfCast>
)

class ProductOfCast(
    val id: Int,
    val media_type: String,
    val title: String?,
    val name: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val character: String?,
    val popularity: Double
)

class ProductOfCastItem(val productOfCast: ProductOfCast) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.product_of_cast_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        when (productOfCast.media_type) {
            "movie" -> {
                viewHolder.itemView.poc_title.text = productOfCast.title
            }
            "tv" -> {
                viewHolder.itemView.poc_title.text = productOfCast.name
            }
        }

        if (productOfCast.character != null && productOfCast.character != "") {
            viewHolder.itemView.poc_title.append("\nas ${productOfCast.character}")
        }

        try {
            if (productOfCast.poster_path == null) {
                viewHolder.itemView.poc_poster.setImageResource(R.drawable.logo_accent)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + productOfCast.poster_path)
                    .fit()
                    .into(viewHolder.itemView.poc_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}