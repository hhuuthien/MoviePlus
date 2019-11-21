package com.thien.movieplus

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
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

        when (intent.getIntExtra("type", 0)) {
            0 -> finish()
            1 -> fetch("124045")
            2 -> fetch("124046")
            3 -> fetch("124047")
            4 -> fetch("124053")
            101 -> fetchUserList()
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list_list.layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            try {
                val intent = Intent(this, DetailMovieActivity::class.java)
                val movieItem = item as MovieItemRow
                intent.putExtra("MOVIE_ID", movieItem.movie.id)
                intent.putExtra("MOVIE_POSTER", movieItem.movie.poster_path)
                intent.putExtra("MOVIE_BACKDROP", movieItem.movie.backdrop_path)
                intent.putExtra("MOVIE_TITLE", movieItem.movie.title)
                intent.putExtra("MOVIE_VOTE", movieItem.movie.vote_average)
                intent.putExtra("MOVIE_DATE", movieItem.movie.release_date)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(this, DetailShowActivity::class.java)
                val showItem = item as ShowItemRow
                intent.putExtra("SHOW_ID", showItem.show.id)
                intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
                intent.putExtra("SHOW_TITLE", showItem.show.name)
                intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
                intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
                startActivity(intent)
            }
        }

        adapter.setOnItemLongClickListener { item, _ ->
            try {
                val movieItem = item as MovieItemRow
                if (movieItem.movie.overview.equals("Mô tả phim đang được cập nhật")) {
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("Xoá phim \"${movieItem.movie.title}\" khỏi list?")
                        .setPositiveButton("Xoá") { _, _ ->
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val database = FirebaseDatabase.getInstance()
                            val ref = database
                                .getReference(currentUser!!.uid).child("list").child(
                                    intent.getStringExtra("listID")!!
                                ).child("movies").child(movieItem.movie.id.toString())
                            ref.removeValue().addOnCompleteListener {
                                Toast.makeText(
                                    this,
                                    "Đã xoá",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                this.recreate()
                            }
                        }
                        .setNegativeButton("Huỷ bỏ") { _, _ ->
                            return@setNegativeButton
                        }
                        .setCancelable(true)
                        .create()
                    dialog.show()
                }
            } catch (e: Exception) {
                val showItem = item as ShowItemRow
                if (showItem.show.overview.equals("Mô tả TV show đang được cập nhật")) {
                    val dialog = AlertDialog.Builder(this)
                        .setMessage("Xoá show \"${showItem.show.name}\" khỏi list?")
                        .setPositiveButton("Xoá") { _, _ ->
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val database = FirebaseDatabase.getInstance()
                            val ref = database
                                .getReference(currentUser!!.uid).child("list").child(
                                    intent.getStringExtra("listID")!!
                                ).child("shows").child(showItem.show.id.toString())
                            ref.removeValue().addOnCompleteListener {
                                Toast.makeText(
                                    this,
                                    "Đã xoá",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                this.recreate()
                            }
                        }
                        .setNegativeButton("Huỷ bỏ") { _, _ ->
                            return@setNegativeButton
                        }
                        .setCancelable(true)
                        .create()
                    dialog.show()
                }
            }
            false
        }
    }

    private fun fetchUserList() {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        val listId = intent.getStringExtra("listID")
        if (listId != null && listId.isNotEmpty()) {
            val listName = intent.getStringExtra("listName")
            if (listName != null && listName.isNotEmpty()) supportActionBar?.title = listName

            val ref =
                FirebaseDatabase.getInstance()
                    .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("list")
                    .child(listId).child("movies")

            val listener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (p in p0.children) {
                        val string = p.value.toString()
                        val movieTitle =
                            string.substringAfter("title=").substringBefore(", poster=")
                        val movieId = string.substringAfter("id=").substringBefore(", title=")
                        val moviePoster =
                            string.substringAfter("poster=").substringBefore(", vote=")
                        val movieBackdrop =
                            string.substringAfter("backdrop=").substringBefore(", id=")
                        val movieDate =
                            string.substringAfter("date=").substringBefore(", backdrop=")
                        val movieVote = string.substringAfter("vote=").substringBeforeLast("}")
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
                        adapter.add(MovieItemRow(movie))
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
                        adapter.add(ShowItemRow(show))
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

    private fun fetch(type: String) {
        list_list.visibility = INVISIBLE
        list_progress.visibility = VISIBLE

        val url =
            "https://api.themoviedb.org/3/list/$type?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    list_progress.visibility = INVISIBLE
                    Snackbar
                        .make(list_layout, "Không có kết nối", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Thử lại") {
                            fetch(type)
                        }
                        .setActionTextColor(Color.WHITE)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailList = gSon.fromJson(body, MyList::class.java)

                list.clear()
                list = detailList.items

                runOnUiThread {
                    supportActionBar?.title = detailList.name

                    adapter.clear()
                    for (m in list) {
                        adapter.add(MovieItemRow(m))
                    }
                    list_list.adapter = adapter

                    list_list.visibility = VISIBLE
                    list_progress.visibility = INVISIBLE
                }
            }
        })
    }

    private fun isNetworkConnected(): Boolean {
        val cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < 23) {
            val ni = cm.activeNetworkInfo
            if (ni != null) {
                return ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
            }
        } else {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (!isNetworkConnected()) {
            startActivity(Intent(this, OoopsActivity::class.java))
        }
    }
}

class MyList(
    val name: String,
    val items: ArrayList<Movie>
)
