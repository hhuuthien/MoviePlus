package com.thien.movieplus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_detail_movie.*
import kotlinx.android.synthetic.main.list_create_layout.view.*
import kotlinx.android.synthetic.main.list_layout_item.view.*
import kotlinx.android.synthetic.main.progress.view.*
import okhttp3.*
import java.io.IOException

class DetailMovieActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var originalLanguage = ""

    companion object {
        var exist: Boolean = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_movie_nav_info -> dm_view_pager.currentItem = 0
            R.id.bottom_movie_nav_cast -> dm_view_pager.currentItem = 1
            R.id.bottom_movie_nav_image -> dm_view_pager.currentItem = 2
        }
        return true
    }

    private var movieId: Int = -1
    private var posterPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_movie)

        movieId = intent.getIntExtra("MOVIE_ID", -1)
        if (movieId == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Available Info
            val moviePoster = intent.getStringExtra("MOVIE_POSTER")
            if (moviePoster == null || moviePoster.isEmpty()) {
                dm_poster.setImageResource(R.drawable.logo_blue)
            } else {
                posterPath = moviePoster
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500$moviePoster")
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(dm_poster)
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500$moviePoster")
                    .transform(BlurTransformation(this, 22, 1))
                    .into(blurImageView)
            }

            val movieTitle = intent.getStringExtra("MOVIE_TITLE")
            if (movieTitle != null && movieTitle.isNotEmpty()) {
                dm_title.text = movieTitle
            }

            val movieDate = intent.getStringExtra("MOVIE_DATE")
            if (movieDate == null || movieDate.isEmpty()) {
                dm_date.text = null
            } else {
                val day = movieDate.substring(8, 10)
                val month = movieDate.substring(5, 7)
                val year = movieDate.substring(0, 4)
                dm_date.text = "$day-$month-$year"
            }

            fetch(movieId.toString())
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

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(fdminfo)
        adapter.addFrag(fdmcast)
        adapter.addFrag(fdmimage)
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
                }
            }
        })

        dm_title.setOnClickListener {
            Toast.makeText(this, "ID: $movieId", Toast.LENGTH_LONG).show()
        }

        fab_trailer.setOnClickListener {
            fetchTrailer(movieId.toString())
        }

        dm_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            layout_2.visibility = GONE
//        } else {
//            layout_2.visibility = VISIBLE
//            //check if exist
//            database = FirebaseDatabase.getInstance()
//            val myRef = database.getReference(currentUser.uid).child("love_movie")
//
//            val listener = object : ValueEventListener {
//                override fun onDataChange(p0: DataSnapshot) {
//                    val string = p0.toString()
//                    if (string.contains(movieId.toString())) {
//                        dm_love.setImageResource(R.drawable.ic_favorite_yes)
//                        exist = true
//                    } else {
//                        dm_love.setImageResource(R.drawable.ic_favorite_no)
//                        exist = false
//                    }
//                }
//
//                override fun onCancelled(p0: DatabaseError) {}
//            }
//            myRef.addListenerForSingleValueEvent(listener)
//        }

//        dm_love.setOnClickListener {
//            try {
//                if (!exist) {
//                    database = FirebaseDatabase.getInstance()
//                    val myRef = database.getReference(currentUser!!.uid).child("love_movie")
//                        .child(movieId.toString())
//                    myRef.setValue(
//                        Movie(
//                            intent.getStringExtra("MOVIE_POSTER"),
//                            intent.getStringExtra("MOVIE_BACKDROP"),
//                            movieId,
//                            intent.getStringExtra("MOVIE_TITLE"),
//                            intent.getStringExtra("MOVIE_DATE"),
//                            intent.getDoubleExtra("MOVIE_VOTE", -1.0),
//                            ""
//                        )
//                    ).addOnSuccessListener {
//                        Toast.makeText(
//                            this,
//                            "Đã thêm vào danh sách yêu thích",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        dm_love.setImageResource(R.drawable.ic_favorite_yes)
//                        exist = true
//                    }.addOnFailureListener {
//                        Toast.makeText(
//                            this,
//                            "Có lỗi xảy ra. Vui lòng thử lại",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        dm_love.setImageResource(R.drawable.ic_favorite_no)
//                        exist = false
//                    }
//                } else {
//                    database = FirebaseDatabase.getInstance()
//                    val myRef = database.getReference(currentUser!!.uid).child("love_movie")
//                        .child(movieId.toString())
//                    myRef.removeValue().addOnSuccessListener {
//                        Toast.makeText(
//                            this,
//                            "Đã xoá khỏi danh sách yêu thích",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        dm_love.setImageResource(R.drawable.ic_favorite_no)
//                        exist = false
//                    }.addOnFailureListener {
//                        Toast.makeText(
//                            this,
//                            "Có lỗi xảy ra. Vui lòng thử lại",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        dm_love.setImageResource(R.drawable.ic_favorite_yes)
//                        exist = true
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("error", e.toString())
//            }
//        }
//
//        dm_add.setOnClickListener {
//            try {
//                val list = ArrayList<UserList>()
//                val adapter = GroupAdapter<ViewHolder>()
//
//                val myLayout = layoutInflater.inflate(R.layout.list_layout, null)
//
//                val dialog = AlertDialog.Builder(this)
//                    .setView(myLayout)
//                    .create()
//                dialog.show()
//
//                myLayout.layoutlist_create.setOnClickListener {
//                    createList()
//                }
//
//                database = FirebaseDatabase.getInstance()
//                val ref = database.getReference(currentUser!!.uid).child("list")
//                val listener = object : ValueEventListener {
//                    override fun onCancelled(p0: DatabaseError) {
//                    }
//
//                    override fun onDataChange(p0: DataSnapshot) {
//                        adapter.clear()
//                        list.clear()
//
//                        if (p0.hasChildren()) {
//                            myLayout.layoutlist_text.text = "Thêm vào list"
//                            myLayout.layoutlist_create.text = "Tạo list khác"
//                            myLayout.layoutlist_list.visibility = VISIBLE
//                            for (p in p0.children) {
//                                val name =
//                                    p.value.toString()
//                                        .substringAfterLast("name=")
//                                        .substringBefore(", id=")
//                                val id =
//                                    p.value.toString()
//                                        .substringAfterLast("id=")
//                                        .substring(0, 13)
//                                val userList = UserList(id, name)
//                                list.add(userList)
//                                adapter.add(ListItem(userList))
//                            }
//                        } else {
//                            myLayout.layoutlist_text.text = "Chưa có list nào"
//                            myLayout.layoutlist_create.text = "Tạo list mới"
//                            myLayout.layoutlist_list.visibility = GONE
//                        }
//                    }
//                }
//                ref.addValueEventListener(listener)
//
//                val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//                myLayout.layoutlist_list.layoutManager = layoutManager
//
//                myLayout.layoutlist_list.adapter = adapter
//                adapter.notifyDataSetChanged()
//
//                adapter.setOnItemClickListener { item, _ ->
//                    val auth = FirebaseAuth.getInstance()
//                    val data = FirebaseDatabase.getInstance()
//                    val myItem = item as ListItem
//                    val listId = myItem.list.id
//                    val refe = data.getReference(auth.currentUser!!.uid)
//                        .child("list/$listId/movies/$movieId")
//                    refe.setValue(
//                        Movie(
//                            intent.getStringExtra("MOVIE_POSTER"),
//                            intent.getStringExtra("MOVIE_BACKDROP"),
//                            movieId,
//                            intent.getStringExtra("MOVIE_TITLE"),
//                            intent.getStringExtra("MOVIE_DATE"),
//                            intent.getDoubleExtra("MOVIE_VOTE", -1.0),
//                            ""
//                        )
//                    ).addOnCompleteListener {
//                        dialog.dismiss()
//                        val movieName = intent.getStringExtra("MOVIE_TITLE")
//                        val listName = item.list.name
//                        Toast.makeText(
//                            this,
//                            "Đã thêm phim \"$movieName\" vào list \"$listName\"",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("error", e.toString())
//            }
//        }
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
                    Toast.makeText(this, "Đã tạo list \"$name\"", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }
    }

    private fun fetchTrailer(movieId: String) {
        var isTrailer = false

        val myLayout = layoutInflater.inflate(R.layout.progress, null)
        myLayout.p_text.text = "Đang tìm trailer ..."
        val dialog = AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()

        val url = if (originalLanguage == "vi") {
            "https://api.themoviedb.org/3/movie/$movieId/videos?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        } else {
            "https://api.themoviedb.org/3/movie/$movieId/videos?api_key=d4a7514dbdd976453d2679e036009283"
        }

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
                            this@DetailMovieActivity,
                            "Không tìm thấy trailer",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun fetch(movieId: String) {
        //to get IMDB ID
        val url1 =
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request1 = Request.Builder().url(url1).build()
        val client1 = OkHttpClient()
        client1.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovie::class.java)

                originalLanguage =
                    if (detailMovie.original_language == null || detailMovie.original_language == "") {
                        ""
                    } else {
                        detailMovie.original_language
                    }
                Log.d("thienthien", originalLanguage)

                val imdbId = detailMovie.imdb_id
                runOnUiThread {
                    if (imdbId != null) {
                        fetchRapidAPI(imdbId)
                    } else {
                        if (detailMovie.runtime == null || detailMovie.runtime == 0) {
                            dm_time.text = "-"
                        } else {
                            dm_time.text = "${detailMovie.runtime} phút"
                        }

                        dm_star.text = "-"
                        dm_star2.visibility = GONE
                    }
                }
            }
        })
    }

    private fun fetchRapidAPI(imdb_id: String) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://movie-database-imdb-alternative.p.rapidapi.com/?i=$imdb_id&r=json")
            .get()
            .addHeader("x-rapidapi-host", "movie-database-imdb-alternative.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "c9056de874msh171226ab4868aecp195d81jsn711cc91d5756")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovieRapidAPI::class.java)

                runOnUiThread {
                    try {
                        if (detailMovie.Runtime == null || detailMovie.Runtime == "N/A") {
                            dm_time.text = "-"
                        } else {
                            val runtime = detailMovie.Runtime.replace("min", "phút")
                            dm_time.text = runtime
                        }

                        if (detailMovie.imdbRating == null || detailMovie.imdbRating == "N/A") {
                            dm_star.text = "-"
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

class ListItem(val list: UserList) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.list_layout_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameOfList.text = list.name
    }
}