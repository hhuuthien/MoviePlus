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
import kotlinx.android.synthetic.main.list_create_layout.view.*
import kotlinx.android.synthetic.main.movie_item.view.*

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var list = ArrayList<MovieLove>()
    private val adapter = GroupAdapter<ViewHolder>()
    private var listShow = ArrayList<Show>()
    private val adapterShow = GroupAdapter<ViewHolder>()
    private var listList = ArrayList<UserList>()
    private val adapterList = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.title = "Trang cá nhân"

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        acc_love_list.layoutManager = layoutManager

        val layoutManager3 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        acc_loveshow_list.layoutManager = layoutManager3

        val layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        acc_list_list.layoutManager = layoutManager2

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

        adapterShow.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterList.setOnItemClickListener { item, _ ->
            val myItem = item as ListItem
            startActivity(
                Intent(this, ListActivity::class.java)
                    .putExtra("type", 101)
                    .putExtra("listID", myItem.list.id)
                    .putExtra("listName", myItem.list.name)
            )
        }

        adapterList.setOnItemLongClickListener { item, _ ->
            val myItem = item as ListItem
            val dialog = AlertDialog.Builder(this)
                .setMessage("Chọn hành động")
                .setPositiveButton("Xoá") { _, _ ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val database = FirebaseDatabase.getInstance()
                    val ref = database
                        .getReference(currentUser!!.uid).child("list").child(myItem.list.id)
                    ref.removeValue().addOnCompleteListener {
                        Toast.makeText(this, "Đã xoá list ${myItem.list.name}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                .setNegativeButton("Đổi tên") { _, _ ->
                    val myLayout = layoutInflater.inflate(R.layout.list_create_layout, null)
                    myLayout.listcreate_name.setText(myItem.list.name)
                    myLayout.listcreate_name.hint = "Nhập tên mới"
                    myLayout.listcreate_ok.text = "Đổi tên"
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(myLayout)
                        .create()
                    dialog.show()

                    myLayout.listcreate_ok.setOnClickListener {
                        val user = FirebaseAuth.getInstance().currentUser
                        val database = FirebaseDatabase.getInstance()

                        val name = myLayout.listcreate_name.text.toString().trim()
                        val ref =
                            database.getReference(user!!.uid).child("list").child(myItem.list.id)
                                .child("name")
                        ref.setValue(name).addOnSuccessListener {
                            Toast.makeText(this, "Đã đổi tên", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }
                }.setNeutralButton("Huỷ bỏ") { _, _ ->
                    return@setNeutralButton
                }
                .setCancelable(true)
                .create()
            dialog.show()
            false
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
            acc_loveshow_layout.visibility = GONE
            acc_list_layout.visibility = GONE
        } else {
            acc_love_layout.visibility = VISIBLE
            acc_loveshow_layout.visibility = VISIBLE
            acc_list_layout.visibility = VISIBLE
            try {
                fetch()
                fetchShow()
                fetchList()
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        }
    }

    private fun fetchShow() {
        listShow.clear()
        adapterShow.clear()

        val currentUser = auth.currentUser
        val myRef =
            FirebaseDatabase.getInstance().getReference(currentUser!!.uid).child("love_show")

        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.childrenCount != 0.toLong()) {
                    acc_loveshow_list.visibility = VISIBLE
                    acc_loveshow_noti.visibility = GONE
                } else {
                    acc_loveshow_list.visibility = GONE
                    acc_loveshow_noti.visibility = VISIBLE
                }

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
                        ""
                    )
                    listShow.add(show)
                    adapterShow.add(ShowItem(show))
                }
            }
        }
        myRef.addValueEventListener(listener)
        acc_loveshow_list.adapter = adapterShow
        adapterShow.notifyDataSetChanged()
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

    private fun fetchList() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val data = FirebaseDatabase.getInstance()
        val ref = data.getReference(currentUser!!.uid).child("list")

        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                listList.clear()
                adapterList.clear()

                if (p0.childrenCount != 0.toLong()) {
                    acc_list_list.visibility = VISIBLE
                    acc_list_noti.visibility = GONE
                } else {
                    acc_list_list.visibility = GONE
                    acc_list_noti.visibility = VISIBLE
                }

                for (p in p0.children) {
                    val string = p.value.toString()
                    val listName = string.substringAfterLast("name=").substringBefore(", id=")
                    val listId = string.substringAfterLast("id=").substringBefore("}")
                    val userList = UserList(listId, listName)
                    listList.add(userList)
                    adapterList.add(ListItem(userList))
                }
            }
        }
        ref.addValueEventListener(listener)
        acc_list_list.adapter = adapterList
        adapterList.notifyDataSetChanged()
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
