package com.thien.movieplus

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.movie_item.view.*


class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var list = ArrayList<MovieLove>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.title = "Trang cá nhân"

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        acc_love_list.layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            val myItem = item as MovieLoveItem
            startActivity(
                Intent(this, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote)
                    .putExtra("MOVIE_DATE", myItem.movie.date)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val dialog = AlertDialog.Builder(this)
                .setMessage("Bạn cần đăng nhập để xem trang cá nhân")
                .setPositiveButton("Đăng nhập") { _, _ ->
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                .setNegativeButton("Huỷ bỏ") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .create()
            dialog.show()

            acc_love_layout.visibility = GONE
        } else {
            acc_love_layout.visibility = VISIBLE
            fetch()
        }
    }

    private fun fetch() {
        list.clear()
        adapter.clear()

        val currentUser = auth.currentUser
        database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(currentUser!!.uid).child("love_movie")

        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.childrenCount != 0.toLong()) {
                    acc_love_list.visibility = VISIBLE
                    acc_love_noti.visibility = GONE
                } else {
                    acc_love_list.visibility = GONE
                    acc_love_noti.visibility = VISIBLE
                }

                for (p in p0.children) {
                    val movieString = p.value.toString()
                    val movieTitle =
                        movieString.substringAfter("title=").substringBefore(", poster=")
                    val movieId = movieString.substringAfter("id=").substringBefore(", title=")
                    val moviePoster =
                        movieString.substringAfter("poster=").substringBefore(", vote=")
                    val movieBackdrop =
                        movieString.substringAfter("backdrop=").substringBefore(", id=")
                    val movieDate =
                        movieString.substringAfter("date=").substringBefore(", backdrop=")
                    val movieVote = movieString.substringAfter("vote=").substringBeforeLast("}")
                    val movie = MovieLove(
                        movieId.toInt(),
                        movieTitle,
                        moviePoster,
                        movieBackdrop,
                        movieVote.toDouble(),
                        movieDate
                    )
                    list.add(movie)
                    adapter.add(MovieLoveItem(movie))
                }
            }
        }
        myRef.addValueEventListener(listener)
        acc_love_list.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_account, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_signout -> {
                auth.signOut()
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class MovieLoveItem(val movie: MovieLove) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.m_title.text = movie.title
        try {
            if (movie.poster == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + movie.poster)
                    .placeholder(R.drawable.logo_accent)
                    .noFade()
                    .into(viewHolder.itemView.m_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
