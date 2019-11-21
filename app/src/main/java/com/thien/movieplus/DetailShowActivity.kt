package com.thien.movieplus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_detail_show.*
import kotlinx.android.synthetic.main.list_create_layout.view.*
import kotlinx.android.synthetic.main.list_layout.view.*
import kotlinx.android.synthetic.main.progress.view.*
import okhttp3.*
import java.io.IOException

class DetailShowActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_show_nav_info -> ds_view_pager.currentItem = 0
            R.id.bottom_show_nav_cast -> ds_view_pager.currentItem = 1
            R.id.bottom_show_nav_image -> ds_view_pager.currentItem = 2
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
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

            val showVote = intent.getDoubleExtra("SHOW_VOTE", -1.0)
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
        bundle.putInt("s_id", showId)

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
            ) {
            }

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
            slayout_2.visibility = View.GONE
        } else {
            slayout_2.visibility = View.VISIBLE
            //check if exist
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference(currentUser.uid).child("love_show")

            val listener = object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val string = p0.toString()
                    if (string.contains(showId.toString())) {
                        ds_love.setImageResource(R.drawable.ic_favorite_yes)
                        exist = true
                    } else {
                        ds_love.setImageResource(R.drawable.ic_favorite_no)
                        exist = false
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            }
            myRef.addListenerForSingleValueEvent(listener)
        }

        ds_love.setOnClickListener {
            try {
                if (!exist) {
                    val myRef = FirebaseDatabase.getInstance().getReference(currentUser!!.uid)
                        .child("love_show")
                        .child(showId.toString())
                    myRef.setValue(
                        Show(
                            intent.getStringExtra("SHOW_POSTER"),
                            showId,
                            intent.getStringExtra("SHOW_TITLE"),
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
                        ds_love.setImageResource(R.drawable.ic_favorite_yes)
                        exist = true
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Có lỗi xảy ra. Vui lòng thử lại",
                            Toast.LENGTH_LONG
                        ).show()
                        ds_love.setImageResource(R.drawable.ic_favorite_no)
                        exist = false
                    }
                } else {
                    val myRef = FirebaseDatabase.getInstance().getReference(currentUser!!.uid)
                        .child("love_show")
                        .child(showId.toString())
                    myRef.removeValue().addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Đã xoá khỏi danh sách yêu thích",
                            Toast.LENGTH_LONG
                        ).show()
                        ds_love.setImageResource(R.drawable.ic_favorite_no)
                        exist = false
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Có lỗi xảy ra. Vui lòng thử lại",
                            Toast.LENGTH_LONG
                        ).show()
                        ds_love.setImageResource(R.drawable.ic_favorite_yes)
                        exist = true
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        }

        ds_add.setOnClickListener {
            try {
                val list = ArrayList<UserList>()
                val adapterter = GroupAdapter<ViewHolder>()

                val myLayout = layoutInflater.inflate(R.layout.list_layout, null)

                val dialog = AlertDialog.Builder(this)
                    .setView(myLayout)
                    .create()
                dialog.show()

                myLayout.layoutlist_create.setOnClickListener {
                    createList()
                }

                val ref =
                    FirebaseDatabase.getInstance().getReference(currentUser!!.uid).child("list")
                val listener = object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        adapterter.clear()
                        list.clear()

                        if (p0.hasChildren()) {
                            myLayout.layoutlist_text.text = "Thêm vào list"
                            myLayout.layoutlist_create.text = "Tạo list khác"
                            myLayout.layoutlist_list.visibility = View.VISIBLE
                            for (p in p0.children) {
                                val name =
                                    p.value.toString()
                                        .substringAfterLast("name=")
                                        .substringBefore(", id=")
                                val id =
                                    p.value.toString()
                                        .substringAfterLast("id=")
                                        .substring(0, 13)
                                val userList = UserList(id, name)
                                list.add(userList)
                                adapterter.add(ListItem(userList))
                            }
                        } else {
                            myLayout.layoutlist_text.text = "Chưa có list nào"
                            myLayout.layoutlist_create.text = "Tạo list mới"
                            myLayout.layoutlist_list.visibility = View.GONE
                        }
                    }
                }
                ref.addValueEventListener(listener)

                val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                myLayout.layoutlist_list.layoutManager = layoutManager

                myLayout.layoutlist_list.adapter = adapterter
                adapterter.notifyDataSetChanged()

                adapterter.setOnItemClickListener { item, _ ->
                    val auth = FirebaseAuth.getInstance()
                    val data = FirebaseDatabase.getInstance()
                    val myItem = item as ListItem
                    val listId = myItem.list.id
                    val refe = data.getReference(auth.currentUser!!.uid)
                        .child("list/$listId/shows/$showId")
                    refe.setValue(
                        Show(
                            intent.getStringExtra("SHOW_POSTER"),
                            showId,
                            intent.getStringExtra("SHOW_TITLE"),
                            intent.getDoubleExtra("SHOW_VOTE", -1.0),
                            intent.getStringExtra("SHOW_BACKDROP"),
                            ""
                        )
                    ).addOnCompleteListener {
                        dialog.dismiss()
                        val showName = intent.getStringExtra("SHOW_TITLE")
                        val listName = item.list.name
                        Toast.makeText(
                            this,
                            "Đã thêm \"$showName\" vào list \"$listName\"",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        }
    }

    private fun createList() {
        try {
            val myLayout = layoutInflater.inflate(R.layout.list_create_layout, null)
            val dialog = AlertDialog.Builder(this)
                .setView(myLayout)
                .create()
            dialog.show()

            myLayout.listcreate_ok.setOnClickListener {
                val auth = FirebaseAuth.getInstance()
                val database = FirebaseDatabase.getInstance()

                val name = myLayout.listcreate_name.text.toString().trim()
                val timestamp = System.currentTimeMillis()
                val id = timestamp.toString()

                val ref = database.getReference(auth.currentUser!!.uid).child("list").child(id)
                ref.setValue(UserList(id, name)).addOnSuccessListener {
                    Toast.makeText(this, "Đã tạo list", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }
    }

    private fun fetch(showId: String) {
        val url =
            "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
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
        myLayout.p_text.text = "Đang tìm trailer ..."
        val dialog = AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()

        val url =
            "https://api.themoviedb.org/3/tv/$showId/videos?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
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
                    if (video.site == "YouTube" && video.name.contains("trailer", true)) {
                        isTrailer = true
                        val appIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${video.key}"))
                        val webIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=${video.key}")
                        )
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
                        Toast.makeText(
                            this@DetailShowActivity,
                            "Không tìm thấy trailer",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }
}

class DeShow(
    val backdrop_path: String,
    val episode_run_time: List<Int>,
    val first_air_date: String,
    val genres: ArrayList<Genre>,
    val id: Int,
    val last_air_date: String,
    val name: String,
    val number_of_episodes: Int,
    val original_language: String,
    val overview: String?,
    val poster_path: String,
    val vote_average: Double?,
    val vote_count: Int
)

