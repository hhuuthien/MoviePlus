package com.thien.movieplus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_detail_movie.*
import kotlinx.android.synthetic.main.love_layout.view.*
import okhttp3.*
import java.io.IOException

class DetailMovieActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    companion object {
        var exist: Boolean = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_movie_nav_info -> dm_view_pager.currentItem = 0
            R.id.bottom_movie_nav_cast -> dm_view_pager.currentItem = 1
            R.id.bottom_movie_nav_image -> dm_view_pager.currentItem = 2
            R.id.bottom_movie_nav_video -> dm_view_pager.currentItem = 3
        }
        return true
    }

    private var movieId: Int = -1
    private var posterPath: String = ""
    private var backdropPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_movie)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        dm_navigation.menu

        movieId = intent.getIntExtra("MOVIE_ID", -1)
        if (movieId == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Available Info
            val moviePoster = intent.getStringExtra("MOVIE_POSTER")
            if (moviePoster == null || moviePoster.isEmpty()) {
                dm_poster.setImageResource(R.drawable.logo_accent)
            } else {
                posterPath = moviePoster
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500$moviePoster")
                        .fit()
                        .into(dm_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200$moviePoster")
                        .fit()
                        .into(dm_poster)
                }

                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w200$moviePoster")
                    .transform(BlurTransformation(this, 22, 1))
                    .into(dm_blurImageView)
            }

            val movieBackdrop = intent.getStringExtra("MOVIE_BACKDROP")
            if (movieBackdrop == null || movieBackdrop.isEmpty()) {
                dm_backdrop.setImageResource(R.drawable.logo_accent)
            } else {
                backdropPath = movieBackdrop
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500$movieBackdrop")
                        .fit()
                        .into(dm_backdrop)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200$movieBackdrop")
                        .fit()
                        .into(dm_backdrop)
                }
            }

            val movieTitle = intent.getStringExtra("MOVIE_TITLE")
            if (movieTitle != null && movieTitle.isNotEmpty()) {
                dm_title.text = movieTitle
            }

            val movieDate = intent.getStringExtra("MOVIE_DATE")
            if (movieDate == null || movieDate.isEmpty() || movieDate == "") {
                dm_date.visibility = GONE
            } else {
                val day = movieDate.substring(8, 10)
                val month = movieDate.substring(5, 7)
                val year = movieDate.substring(0, 4)
                dm_date.text = "$day-$month-$year"
            }

            //check if NETFLIX
            val stringID = intent.getStringExtra("NETFLIX_ID")
            if (stringID != null && stringID != "") {
                mnf.visibility = VISIBLE
                dm_backdrop_netflix.visibility = VISIBLE
                try {
                    mnf.setOnClickListener {
                        val id =
                            stringID.substringAfter("movie:$movieId\":\"").substringBefore("\",\"")
                        val link = "https://www.netflix.com/title/$id"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
                }
            } else {
                mnf.visibility = GONE
                dm_backdrop_netflix.visibility = GONE
            }

            fetch(movieId.toString(), english)
        }

        dm_navigation.setOnNavigationItemSelectedListener(this)

        val bundle = Bundle()
        bundle.putInt("m_id", movieId)

        val fdminfo = FragmentDMInfo()
        fdminfo.arguments = bundle
        val fdmcast = FragmentDMCast()
        fdmcast.arguments = bundle
        val fdmimage = FragmentDMImage()
        fdmimage.arguments = bundle
        val fdmvideo = FragmentDMVideo()
        fdmvideo.arguments = bundle

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(fdminfo)
        adapter.addFrag(fdmcast)
        adapter.addFrag(fdmimage)
        adapter.addFrag(fdmvideo)
        dm_view_pager.adapter = adapter

        dm_view_pager.currentItem = 0
        dm_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> dm_navigation.selectedItemId = R.id.bottom_movie_nav_info
                    1 -> dm_navigation.selectedItemId = R.id.bottom_movie_nav_cast
                    2 -> dm_navigation.selectedItemId = R.id.bottom_movie_nav_image
                    3 -> dm_navigation.selectedItemId = R.id.bottom_movie_nav_video
                }
            }
        })

        dm_title.setOnClickListener {
            Toast.makeText(this, "ID: $movieId", Toast.LENGTH_LONG).show()
        }

        dm_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        dm_backdrop.setOnClickListener {
            if (backdropPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", backdropPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            dm_floating.visibility = GONE
        } else {
            dm_floating.visibility = VISIBLE

            dm_floating.setOnClickListener {
                val layoutInflater = layoutInflater.inflate(R.layout.love_layout, null)
                val dialog = AlertDialog.Builder(this)
                    .setView(layoutInflater)
                    .setCancelable(true)
                    .show()
                //check if exist
                database = FirebaseDatabase.getInstance()
                val myRef = database.getReference(currentUser.uid).child("love_movie")

                val listener = object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val string = p0.toString()
                        if (string.contains(movieId.toString())) {
                            layoutInflater.dummytext.text = "Xoá khỏi danh sách yêu thích"
                            layoutInflater.dummytext.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_delete_from_list,
                                0,
                                0,
                                0
                            )
                            exist = true
                        } else {
                            layoutInflater.dummytext.text = "Thêm vào danh sách yêu thích"
                            layoutInflater.dummytext.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_add_to_list,
                                0,
                                0,
                                0
                            )
                            exist = false
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                }
                myRef.addListenerForSingleValueEvent(listener)

                layoutInflater.dummytext.setOnClickListener {
                    try {
                        dialog.dismiss()
                        val myRef2 =
                            FirebaseDatabase.getInstance().getReference(currentUser.uid)
                                .child("love_movie")
                                .child(movieId.toString())
                        if (!exist) {
                            val timestamp = System.currentTimeMillis()
                            myRef2.setValue(
                                Movie(
                                    intent.getStringExtra("MOVIE_POSTER"),
                                    intent.getStringExtra("MOVIE_BACKDROP"),
                                    movieId,
                                    intent.getStringExtra("MOVIE_TITLE")!! + "@#$timestamp",
                                    intent.getStringExtra("MOVIE_DATE"),
                                    intent.getDoubleExtra("MOVIE_VOTE", -1.0),
                                    ""
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Đã thêm vào danh sách yêu thích",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Có lỗi xảy ra. Vui lòng thử lại",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            myRef2.removeValue().addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Đã xoá khỏi danh sách yêu thích",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Có lỗi xảy ra. Vui lòng thử lại",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("error", e.toString())
                    }
                }
            }
        }
    }

    private fun fetch(movieId: String, english: Boolean) {
        //to get IMDB ID and Collection
        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request1 = Request.Builder().url(url).build()
        val client1 = OkHttpClient()
        client1.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovie::class.java)

                val imdbId = detailMovie.imdb_id
                runOnUiThread {
                    if (imdbId != null) {
                        fetchRapidAPI(imdbId, english)
                    } else {
                        if (detailMovie.runtime == null || detailMovie.runtime == 0) {
                            dm_time.visibility = GONE
                        } else {
                            dm_time.text = "${detailMovie.runtime} phút"
                        }

                        dm_star.visibility = GONE
                        dm_star2.visibility = GONE
                    }
                }
            }
        })
    }

    private fun fetchRapidAPI(imdb_id: String, english: Boolean) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://movie-database-imdb-alternative.p.rapidapi.com/?i=$imdb_id&r=json")
            .get()
            .addHeader("x-rapidapi-host", "movie-database-imdb-alternative.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "c9056de874msh171226ab4868aecp195d81jsn711cc91d5756")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovieRapidAPI::class.java)

                runOnUiThread {
                    try {
                        if (detailMovie.Runtime == null || detailMovie.Runtime == "N/A") {
                            dm_time.visibility = GONE
                        } else {
                            dm_time.text = detailMovie.Runtime.replace("min", "phút")
                        }

                        if (detailMovie.imdbRating == null || detailMovie.imdbRating == "N/A") {
                            dm_star.visibility = GONE
                        } else {
                            dm_star.text = detailMovie.imdbRating
                        }

                        if (detailMovie.Metascore == null || detailMovie.Metascore == "N/A") {
                            dm_star2.visibility = GONE
                        } else {
                            dm_star2.text = detailMovie.Metascore
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.d("error", ex.toString())
                    }
                }
            }
        })
    }
}

class VideoResult(
    val results: ArrayList<Video>
)

class Video(
    val name: String,
    val site: String,
    val key: String
)

class UserList(
    val id: String,
    val name: String
)