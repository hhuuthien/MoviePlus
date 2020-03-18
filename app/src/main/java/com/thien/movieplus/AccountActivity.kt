package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.acc_layout.view.*
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.name_layout.view.*

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var linkAvatar = ""
    private var nameGlobal = ""

    private val adapterM = GroupAdapter<ViewHolder>()
    private val adapterS = GroupAdapter<ViewHolder>()
    private val adapterP = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        //layoutManager
        acc_list_lovemovie.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        acc_list_loveshow.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        acc_list_lovepeople.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        acc_info_image.setOnClickListener {
            if (linkAvatar != "") {
                startActivity(
                    Intent(this, PictureActivity::class.java)
                        .putExtra("avatarString", linkAvatar)
                )
            }
        }

        adapterM.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItemSmall
            val intent = Intent(this, DetailMovieActivity::class.java)
                .putExtra("MOVIE_ID", myItem.movie.id)
                .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                .putExtra("MOVIE_DATE", myItem.movie.release_date)
                .putExtra("MOVIE_TITLE", myItem.movie.title)
            startActivity(intent)
        }

        adapterS.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailShowActivity::class.java)
            val showItem = item as ShowItemSmall
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterP.setOnItemClickListener { item, _ ->
            val intent = Intent(this, DetailCastActivity::class.java)
            val myItem = item as PeopleItem
            intent.putExtra("CAST_ID", myItem.people.id)
            intent.putExtra("CAST_NAME", myItem.people.name)
            intent.putExtra("CAST_POSTER", myItem.people.profile_path)
            startActivity(intent)
        }

        acc_floating.setOnClickListener {
            val layoutInflater = layoutInflater.inflate(R.layout.acc_layout, null)
            val dialog = AlertDialog.Builder(this)
                .setView(layoutInflater)
                .setCancelable(true)
                .show()

            layoutInflater.dummytext1.text = "Thay đổi ảnh đại diện"
            layoutInflater.dummytext1.setOnClickListener {
                try {
                    dialog.dismiss()
                    startActivity(
                        Intent(this, PermissionActivity::class.java)
                            .putExtra("type", "toChangeImage")
                    )
                } catch (e: Exception) {
                    Log.d("error", e.toString())
                }
            }

            layoutInflater.dummytext2.text = "Thay đổi tên"
            layoutInflater.dummytext2.setOnClickListener {
                try {
                    dialog.dismiss()
                    changeName()
                } catch (e: Exception) {
                    Log.d("error", e.toString())
                }
            }

            layoutInflater.dummytext3.text = "Đăng xuất"
            layoutInflater.dummytext3.setOnClickListener {
                try {
                    dialog.dismiss()
                    auth.signOut()
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    Log.d("error", e.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, Account2Activity::class.java))
            finish()
        } else {
            val pref = this.getSharedPreferences("SettingPref", 0)
            val english = pref.getBoolean("english", false)
            val goodquality = pref.getBoolean("goodquality", true)

            try {
                fetchImage()
                fetchInfo()
                fetchMovie(goodquality)
                fetchShow(goodquality)
                fetchPeople(goodquality)
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        }
    }

    private fun fetchMovie(goodquality: Boolean) {
        val listM = ArrayList<Movie>()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference(uid).child("love_movie")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (m in p0.children) {
                    val movieString = m.value.toString()
                    val poster = movieString.substringAfter("poster_path=").substringBefore("}")
                    val backdrop =
                        movieString.substringAfter("backdrop_path=").substringBefore(", overview")
                    val id = movieString.substringAfter("id=").substringBefore(", title")
                    val date = movieString.substringAfter("release_date=").substringBefore(", vote")
                    val titleAndTimeStamp =
                        movieString.substringAfter("title=").substringBefore(", poster")
                    //get TIMESTAMP to set to OVERVIEW
                    val title = titleAndTimeStamp.substringBefore("@#")
                    val timestamp = titleAndTimeStamp.substringAfter("@#")

                    val movie = Movie(poster, backdrop, id.toInt(), title, date, -1.0, timestamp)
                    listM.add(movie)
                }
                if (listM.size == 0) {
                    acc_layout_lovemovie.visibility = GONE
                } else {
                    acc_layout_lovemovie.visibility = VISIBLE

                    listM.sortByDescending { it.overview!!.toLong() }
                    adapterM.clear()
                    for (m in listM) {
                        adapterM.add(MovieItemSmall(m, goodquality))
                    }
                    acc_list_lovemovie.adapter = adapterM
                }
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun fetchShow(goodquality: Boolean) {
        val listS = ArrayList<Show>()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference(uid).child("love_show")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (m in p0.children) {
                    val showString = m.value.toString()
                    val poster = showString.substringAfter("poster_path=").substringBefore("}")
                    val backdrop =
                        showString.substringAfter("backdrop_path=").substringBefore(", overview")
                    val id = showString.substringAfter("id=").substringBefore(", poster")
                    val titleAndTimeStamp =
                        showString.substringAfter("name=").substringBefore(", id")
                    //get TIMESTAMP to set to OVERVIEW
                    val title = titleAndTimeStamp.substringBefore("@#")
                    val timestamp = titleAndTimeStamp.substringAfter("@#")

                    val show = Show(poster, id.toInt(), title, -1.0, backdrop, timestamp)
                    listS.add(show)
                }
                if (listS.size == 0) {
                    acc_layout_loveshow.visibility = GONE
                } else {
                    acc_layout_loveshow.visibility = VISIBLE

                    listS.sortByDescending { it.overview!!.toLong() }
                    adapterS.clear()
                    for (m in listS) {
                        adapterS.add(ShowItemSmall(m, goodquality))
                    }
                    acc_list_loveshow.adapter = adapterS
                }
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun fetchPeople(goodquality: Boolean) {
        val listP = ArrayList<People>()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference(uid).child("love_people")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (m in p0.children) {
                    val string = m.value.toString()
                    val name = string.substringAfter("name=").substringBefore("@#")
                    val timestamp = string.substringAfter("@#").substringBefore(", profile_path")
                    val poster = string.substringAfter("profile_path=").substringBefore(", id")
                    val id = string.substringAfter("id=").substringBefore("}")

                    //set timestamp to known_for
                    val people = People(name, poster, id.toInt(), timestamp)
                    listP.add(people)
                }
                if (listP.size == 0) {
                    acc_layout_lovepeople.visibility = GONE
                } else {
                    acc_layout_lovepeople.visibility = VISIBLE

                    listP.sortByDescending { it.known_for_department!!.toLong() }
                    adapterP.clear()
                    for (m in listP) {
                        adapterP.add(PeopleItem(m))
                    }
                    acc_list_lovepeople.adapter = adapterP
                }
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun fetchInfo() {
        val ref =
            FirebaseDatabase.getInstance()
                .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_name")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val name = p0.value.toString()
                if (name == "null" || name == "" || name.length > 30) {
                    acc_info_name.text = "Chào bạn!"
                } else {
                    acc_info_name.text = "Chào bạn, $name!"
                }

                nameGlobal = name
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun fetchImage() {
        val ref =
            FirebaseDatabase.getInstance()
                .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_image")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val link = p0.value.toString()
                if (link == "null" || link == "") {
                    acc_info_image.setImageResource(R.drawable.logo_accent)
                } else {
                    Picasso.get()
                        .load(link)
                        .fit()
                        .into(acc_info_image)
                    linkAvatar = link
                }
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun changeName() {
        val myLayout = layoutInflater.inflate(R.layout.name_layout, null)
        myLayout.cn_name.setText(nameGlobal)

        val dialog = AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()

        myLayout.cn_button.setOnClickListener {
            val newName = myLayout.cn_name.text.toString().trim()
            val ref =
                FirebaseDatabase.getInstance()
                    .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_name")
            ref.setValue(newName).addOnSuccessListener {
                Toast.makeText(this, "Đã đổi tên", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        }
    }
}
