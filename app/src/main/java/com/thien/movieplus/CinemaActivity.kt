package com.thien.movieplus

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_cinema.*
import kotlinx.android.synthetic.main.cinema_item.view.*
import java.io.File
import java.io.FileOutputStream

class CinemaActivity : AppCompatActivity() {

    private var list = ArrayList<Cinema>()
    private val adapterCinema = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cinema)

        supportActionBar?.title = "Rạp chiếu phim"
        copyDB()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_cinema.layoutManager = layoutManager

        val dbHelper = DBHelper(this)

        val listCum = ArrayList<String>()
        listCum.add("BHD")
        listCum.add("CGV")
        listCum.add("Cinestar")
        listCum.add("Galaxy")
        listCum.add("Lotte")
        listCum.add("Cụm rạp khác")
        listCum.add("Tất cả cụm rạp")

        val adapterSpinner = ArrayAdapter(this, R.layout.spinner_item2, listCum)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        recyclerView_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                var list = ArrayList<Cinema>()
                when (i) {
                    0 -> {
                        list = dbHelper.getCinemaByGroup("bhd")
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp BHD."
                    }
                    1 -> {
                        list = dbHelper.getCinemaByGroup("cgv")
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp CGV."
                    }
                    2 -> {
                        list = dbHelper.getCinemaByGroup("cin")
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp Cinestar."
                    }
                    3 -> {
                        list = dbHelper.getCinemaByGroup("glx")
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp Galaxy."
                    }
                    4 -> {
                        list = dbHelper.getCinemaByGroup("lot")
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp Lotte."
                    }
                    5 -> {
                        list = dbHelper.getCinemaOther()
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp chiếu phim khác."
                    } //abc, dcine, mega gs
                    6 -> {
                        list = dbHelper.allCinema as ArrayList<Cinema>
                        recyclerView_text.text = "Tìm thấy tất cả ${list.size} rạp chiếu phim."
                    }
                }

                list.sortWith(compareBy { it.tenrap })

                adapterCinema.clear()
                for (c in list) {
                    adapterCinema.add(CinemaItem(c))
                }
                recyclerView_cinema.adapter = adapterCinema
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        recyclerView_spinner.adapter = adapterSpinner
        recyclerView_spinner.setSelection(6)
    }

    private fun copyDB() {
        //check if databases folder exists, if not exist, create it
        val dbFolder = File(applicationInfo.dataDir + "/databases/")
        if (!dbFolder.exists()) dbFolder.mkdir()

        //check if databases file exists, if not exist, just copy, if exists, delete the old one, then copy
        val dbFile = File(applicationInfo.dataDir + "/databases/movie_plus.db")
        if (dbFile.exists()) dbFile.delete()

        //copy
        try {
            val inputStream = assets.open("movie_plus.db")
            val outputStream =
                FileOutputStream(applicationInfo.dataDir + "/databases/movie_plus.db")
            val buffer = ByteArray(1024)
            var length: Int = inputStream.read(buffer)
            while (length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.d("error_copying_db", e.toString())
        }
    }
}

class CinemaItem(private val cinema: Cinema) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.cinema_item
    }

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            viewHolder.itemView.cine_name.text = cinema.tenrap
            viewHolder.itemView.cine_address.text = cinema.diachi
            viewHolder.itemView.cine_address2.text = cinema.quan + ", " + cinema.thanhpho

            when (cinema.cumrap) {
                "glx" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.glx)
                "cgv" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.cgv)
                "bhd" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.bhd)
                "lot" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.lot)
                "dci" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.dci)
                "cin" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.cin)
                "meg" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.meg)
                "abc" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.logo_blue)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
