package com.thien.movieplus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_movie.*
import kotlinx.android.synthetic.main.activity_detail_show.*
import okhttp3.*
import java.io.IOException

class DetailShowActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_show_nav_info -> ds_view_pager.currentItem = 0
            R.id.bottom_show_nav_cast -> ds_view_pager.currentItem = 1
            R.id.bottom_show_nav_image -> ds_view_pager.currentItem = 2
        }
        return true
    }

    private var showId: Int = -1
    private var posterPath : String = ""
    private var backdropPath : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_show)

        showId = intent.getIntExtra("SHOW_ID", -1)
        if (showId == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Available Info
            val showPoster = intent.getStringExtra("SHOW_POSTER")
            if (showPoster == null || showPoster.isEmpty()) {
                ds_poster.setImageResource(R.drawable.logo_blue)
            } else {
                posterPath = showPoster
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300$showPoster")
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(ds_poster)
            }

            val showBackdrop = intent.getStringExtra("SHOW_BACKDROP")
            if (showBackdrop == null || showBackdrop.isEmpty()) {
                ds_backdrop.setImageResource(R.drawable.logo_blue)
            } else {
                backdropPath = showBackdrop
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500$showBackdrop")
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(ds_backdrop)
            }

            val showTitle = intent.getStringExtra("SHOW_TITLE")
            if (showTitle != null && showTitle.isNotEmpty()) {
                ds_title.text = showTitle
            }

            val showVote = intent.getDoubleExtra("SHOW_VOTE",-1.0)
            if (showVote == -1.0 || showVote == 0.0) {
                ds_score.percent = 0F
                ds_score_text.text = ""
            } else {
                ds_score.percent = (showVote * 10).toFloat()
                ds_score_text.text = showVote.toString()
            }

            fetch(showId.toString())
        }

        ds_navigation.setOnNavigationItemSelectedListener(this)

        val bundle = Bundle()
        bundle.putInt("s_id",showId)

        val fdsinfo = FragmentDSInfo()
        fdsinfo.arguments = bundle
        val fdscast = FragmentDSCast()
        fdscast.arguments = bundle
        val fdsimage = FragmentDSImage()
        fdsimage.arguments = bundle

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(fdsinfo)
        adapter.addFrag(fdscast)
        adapter.addFrag(fdsimage)
        ds_view_pager.adapter = adapter

        ds_view_pager.currentItem = 0
        ds_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_info
                    1 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_cast
                    2 -> ds_navigation.selectedItemId = R.id.bottom_show_nav_image
                }
            }
        })

        ds_title.setOnClickListener {
            Toast.makeText(this, "ID: $showId", Toast.LENGTH_LONG).show()
        }

        ds_trailer.setOnClickListener {
            fetchTrailer(showId.toString())
        }

        ds_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }

        ds_backdrop.setOnClickListener {
            if (backdropPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", backdropPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }
    }

    private fun fetch(showId: String) {
        val url = "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailShow = gSon.fromJson(body, DeShow::class.java)

                runOnUiThread {
                    for (g in detailShow.genres) {
                        ds_genre_info.append(g.name + "  ")
                    }
                }
            }
        })
    }

    private fun fetchTrailer(showId: String) {
        var isTrailer = false

        val myLayout = layoutInflater.inflate(R.layout.progress, null)
        val dialog = AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()

        val url = "https://api.themoviedb.org/3/tv/$showId/videos?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, VideoResult::class.java)

                val listVideo = result.results
                for (video in listVideo) {
                    if (video.site == "YouTube" && video.name.contains("trailer",true)) {
                        isTrailer = true
                        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${video.key}"))
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=${video.key}"))
                        try {
                            startActivity(appIntent)
                            dialog.dismiss()
                            return
                        } catch (ex: ActivityNotFoundException) {
                            startActivity(webIntent)
                            dialog.dismiss()
                            return
                        }
                    }
                }
                if (!isTrailer) {
                    runOnUiThread {
                        dialog.dismiss()
                        Toast.makeText(this@DetailShowActivity,"Không tìm thấy trailer",Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}

class DeShow (
    val backdrop_path : String,
    val episode_run_time : List<Int>,
    val first_air_date:String,
    val genres:ArrayList<Genre>,
    val id:Int,
    val last_air_date:String,
    val name:String,
    val number_of_episodes:Int,
    val original_language:String,
    val overview:String?,
    val poster_path:String,
    val vote_average:Double?,
    val vote_count:Int
)

