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
import kotlinx.android.synthetic.main.activity_detail_show.*
import kotlinx.android.synthetic.main.love_layout.view.*
import okhttp3.*
import java.io.IOException

class DetailShowActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_show_nav_info -> ds_view_pager.currentItem = 0
            R.id.bottom_show_nav_cast -> ds_view_pager.currentItem = 1
            R.id.bottom_show_nav_image -> ds_view_pager.currentItem = 2
            R.id.bottom_show_nav_season -> ds_view_pager.currentItem = 3
        }
        return true
    }

    private var showId: Int = -1
    private var posterPath: String = ""
    private var backdropPath: String = ""

    companion object {
        var exist: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_show)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        showId = intent.getIntExtra("SHOW_ID", -1)
        if (showId == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Available Info
            val showPoster = intent.getStringExtra("SHOW_POSTER")
            if (showPoster == null || showPoster.isEmpty()) {
                ds_poster.setImageResource(R.drawable.logo_accent)
            } else {
                posterPath = showPoster
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500$showPoster")
                        .fit()
                        .into(ds_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200$showPoster")
                        .fit()
                        .into(ds_poster)
                }

                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w200$showPoster")
                    .transform(BlurTransformation(this, 22, 1))
                    .into(ds_blurImageView)
            }

            val showBackdrop = intent.getStringExtra("SHOW_BACKDROP")
            if (showBackdrop == null || showBackdrop.isEmpty()) {
                ds_backdrop.setImageResource(R.drawable.logo_accent)
            } else {
                backdropPath = showBackdrop
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500$showBackdrop")
                        .fit()
                        .into(ds_backdrop)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200$showBackdrop")
                        .fit()
                        .into(ds_backdrop)
                }
            }

            val showTitle = intent.getStringExtra("SHOW_TITLE")
            if (showTitle != null && showTitle.isNotEmpty()) {
                ds_title.text = showTitle
            }

            //check if NETFLIX
            val stringID = intent.getStringExtra("NETFLIX_ID")
            if (stringID != null && stringID != "") {
                nf.visibility = VISIBLE
                ds_backdrop_netflix.visibility = VISIBLE
                try {
                    nf.setOnClickListener {
                        val id = stringID.substringAfter("tv:$showId\":\"").substringBefore("\",\"")
                        val link = "https://www.netflix.com/title/$id"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
                }
            } else {
                nf.visibility = GONE
                ds_backdrop_netflix.visibility = GONE
            }

            fetchExID(showId.toString())
        }

        ds_navigation.setOnNavigationItemSelectedListener(this)

        val bundle = Bundle()
        bundle.putInt("s_id", showId)

        val fdsinfo = FragmentDSInfo()
        fdsinfo.arguments = bundle
        val fdscast = FragmentDSCast()
        fdscast.arguments = bundle
        val fdsimage = FragmentDSImage()
        fdsimage.arguments = bundle
        val fdssaeason = FragmentDSSeason()
        fdssaeason.arguments = bundle

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(fdsinfo)
        adapter.addFrag(fdscast)
        adapter.addFrag(fdsimage)
        adapter.addFrag(fdssaeason)
        ds_view_pager.adapter = adapter

        ds_view_pager.currentItem = 0
        ds_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_info
                    1 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_cast
                    2 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_image
                    3 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_season
                }
            }
        })

        ds_title.setOnClickListener {
            Toast.makeText(this, "ID: $showId", Toast.LENGTH_LONG).show()
        }

        ds_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        ds_backdrop.setOnClickListener {
            if (backdropPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", backdropPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            ds_floating.visibility = GONE
        } else {
            ds_floating.visibility = VISIBLE

            ds_floating.setOnClickListener {
                val layoutInflater = layoutInflater.inflate(R.layout.love_layout, null)
                val dialog = AlertDialog.Builder(this)
                    .setView(layoutInflater)
                    .setCancelable(true)
                    .show()

                //check if exist
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference(currentUser.uid).child("love_show")

                val listener = object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val string = p0.toString()
                        if (string.contains(showId.toString())) {
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
                                .child("love_show")
                                .child(showId.toString())
                        if (!exist) {
                            val timestamp = System.currentTimeMillis()
                            myRef2.setValue(
                                Show(
                                    intent.getStringExtra("SHOW_POSTER"),
                                    showId,
                                    intent.getStringExtra("SHOW_TITLE") + "@#$timestamp",
                                    intent.getDoubleExtra("SHOW_VOTE", -1.0),
                                    intent.getStringExtra("SHOW_BACKDROP"),
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

    private fun fetchExID(id: String) {
        val url =
            "https://api.themoviedb.org/3/tv/$id/external_ids?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ShowID::class.java)

                runOnUiThread {
                    val imdb = result.imdb_id
                    if (imdb == null || imdb == "") {
                        ds_star.visibility = GONE
                    } else {
                        fetchRapidAPI(imdb)
                    }
                }
            }
        })
    }

    private fun fetchRapidAPI(imdb: String) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://movie-database-imdb-alternative.p.rapidapi.com/?i=$imdb&r=json")
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
                val result = gSon.fromJson(body, DetailShowRapidAPI::class.java)

                runOnUiThread {
                    try {
                        if (result.imdbRating == null || result.imdbRating == "N/A") {
                            ds_star.visibility = GONE
                        } else {
                            ds_star.text = result.imdbRating
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.d("error", ex.toString())
                    }
                }
            }
        })
    }
}

class DeShow(
    val backdrop_path: String,
    val first_air_date: String,
    val id: Int,
    val last_episode_to_air: Ep?,
    val next_episode_to_air: Ep?,
    val name: String?,
    val number_of_episodes: Int,
    val overview: String?,
    val poster_path: String,
    val homepage: String?,
    val seasons: ArrayList<Season>,
    val production_companies: ArrayList<Company>?
)

class ShowID(
    val imdb_id: String?
)

class DetailShowRapidAPI(
    val Director: String?,
    val Genre: String?,
    val imdbRating: String?
)