package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_list.*
import okhttp3.*
import java.io.IOException

class ListActivity : AppCompatActivity() {

    private var list = ArrayList<Movie>()
    private var listS = ArrayList<Show>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val pref = this.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        when (intent.getIntExtra("type", 0)) {
            0 -> finish()
            1 -> {
                fetch("124045", english, goodquality)
                list_img.setImageResource(R.drawable.marvel)
            }
            2 -> {
                fetch("124046", english, goodquality)
                list_img.setImageResource(R.drawable.vietnam)
            }
            3 -> {
                fetch("124047", english, goodquality)
                list_img.setImageResource(R.drawable.pixar)
            }
            4 -> {
                fetch("124053", english, goodquality)
                list_img.setImageResource(R.drawable.oscar)
            }
            5 -> {
                fetch("134631", english, goodquality)
                list_img.setImageResource(R.drawable.oscar2)
            }
            6 -> {
                fetch("134629", english, goodquality)
                list_img.setImageResource(R.drawable.top20)
            }
            7 -> {
                fetch("134632", english, goodquality)
                list_img.setImageResource(R.drawable.imdblist)
            }
            8 -> {
                fetch("134633", english, goodquality)
                list_img.setImageResource(R.drawable.animation)
            }
            101 -> fetchUserList(goodquality)
        }

        val layoutManager = GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        list_list.layoutManager = layoutManager

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
                val intent = Intent(this, DetailShowActivity::class.java)
                val showItem = item as ShowItemSmall
                intent.putExtra("SHOW_ID", showItem.show.id)
                intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
                intent.putExtra("SHOW_TITLE", showItem.show.name)
                intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
                intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
                startActivity(intent)
            }
        }
    }

    private fun fetchUserList(goodquality: Boolean) {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        val listId = intent.getStringExtra("listID")
        if (listId != null && listId.isNotEmpty()) {
            val listName = intent.getStringExtra("listName")

            val ref =
                FirebaseDatabase.getInstance()
                    .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("list")
                    .child(listId).child("movies")

            val listener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (p in p0.children) {
                        val movieString = p.value.toString()
                        val movieTitle =
                            movieString.substringAfter("title=").substringBefore(", poster_path=")
                        val movieId = movieString.substringAfter("id=").substringBefore(", title=")
                        val moviePoster =
                            movieString.substringAfter("poster_path=").substringBeforeLast("}")
                        val movieBackdrop =
                            movieString.substringAfter("backdrop_path=")
                                .substringBefore(", overview=")
                        val movieDate =
                            movieString.substringAfter("release_date=")
                                .substringBefore(", vote_average=")
                        val movieVote =
                            movieString.substringAfter("vote_average=").substringBefore(", id=")
                        val movie = Movie(
                            moviePoster,
                            movieBackdrop,
                            movieId.toInt(),
                            movieTitle,
                            movieDate,
                            movieVote.toDouble(),
                            "Mô tả phim đang được cập nhật"
                        )
                        list.add(movie)
                        adapter.add(MovieItemRow(movie, goodquality))
                    }
                }
            }
            ref.addValueEventListener(listener)

            val ref2 =
                FirebaseDatabase.getInstance()
                    .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("list")
                    .child(listId).child("shows")

            val listener2 = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (p in p0.children) {
                        val string = p.value.toString()
                        val showTitle =
                            string.substringAfter("name=").substringBefore(", id=")
                        val showId = string.substringAfter("id=").substringBefore(", poster_path=")
                        val showPoster =
                            string.substringAfter("poster_path=").substringBeforeLast("}")
                        val showBackdrop =
                            string.substringAfter("backdrop_path=").substringBefore(", overview=")
                        val showVote =
                            string.substringAfter("vote_average=").substringBeforeLast(", name=")
                        val show = Show(
                            showPoster,
                            showId.toInt(),
                            showTitle,
                            showVote.toDouble(),
                            showBackdrop,
                            "Mô tả TV show đang được cập nhật"
                        )
                        listS.add(show)
                        adapter.add(ShowItemRow(show, goodquality))
                    }
                }
            }
            ref2.addValueEventListener(listener2)

            list_list.adapter = adapter
            adapter.notifyDataSetChanged()

            list_list.visibility = VISIBLE
            list_progress.visibility = INVISIBLE

        } else {
            Toast.makeText(this, "Có lỗi xảy ra. Vui lòng thử lại", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun fetch(type: String, english: Boolean, goodquality: Boolean) {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailList = gSon.fromJson(body, MyList::class.java)

                list.clear()
                list = detailList.items

                runOnUiThread {
                    list_name.text = detailList.name

                    adapter.clear()
                    for (m in list) {
                        adapter.add(MovieItemSmall(m, goodquality))
                    }
                    list_list.adapter = adapter

                    list_list.visibility = VISIBLE
                    list_progress.visibility = INVISIBLE
                }
            }
        })
    }
}

class MyList(
    val name: String,
    val items: ArrayList<Movie>
)

class MyListV4(
    val name: String,
    val results: ArrayList<Movie>
)

class MyListShow(
    val name: String,
    val items: ArrayList<Show>
)

class MyListShowV4(
    val name: String,
    val results: ArrayList<Show>
)