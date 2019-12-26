package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
        val dbHelper = DBHelper(this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_cinema.layoutManager = layoutManager

        val listCum = ArrayList<String>()
        listCum.add("Tất cả rạp")
        listCum.add("BHD")
        listCum.add("CGV")
        listCum.add("Cinestar")
        listCum.add("Dcine")
        listCum.add("Galaxy")
        listCum.add("Lotte")
        listCum.add("Mega GS")

        val listCity = ArrayList<String>()
        listCity.add("Bến Tre")
        listCity.add("Bình Dương")
        listCity.add("Hồ Chí Minh")
        listCity.add("Tiền Giang")

        val adapter = ArrayAdapter(this, R.layout.spinner_item, listCum)
        adapter.setDropDownViewResource(R.layout.spinner_item_choice)
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item, listCity)
        adapter2.setDropDownViewResource(R.layout.spinner_item_choice)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                var cum = listCum[i]
                val tencum = cum
                val city = listCity[spinner2.selectedItemPosition]

                when (cum) {
                    "BHD" -> cum = "bhd"
                    "CGV" -> cum = "cgv"
                    "Cinestar" -> cum = "cin"
                    "Dcine" -> cum = "dci"
                    "Galaxy" -> cum = "glx"
                    "Lotte" -> cum = "lot"
                    "Mega GS" -> cum = "meg"
                }

                list = if (cum == "Tất cả rạp") {
                    dbHelper.getCinemaALLbyCity(city)
                } else {
                    dbHelper.getCinemaALL(cum, city)
                }

                if (list.size == 0) {
                    recyclerView_cinema.visibility = GONE
                    text_noti.visibility = VISIBLE
                    text_noti.text = "Không tìm thấy rạp nào"
                } else {
                    recyclerView_cinema.visibility = VISIBLE
                    text_noti.visibility = GONE

                    list.sortedWith(compareBy { it.tenrap })
                    adapterCinema.clear()
                    for (m in list) {
                        adapterCinema.add(CinemaItem(m))
                    }
                    recyclerView_cinema.adapter = adapterCinema
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(3)


        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                var cum = listCum[spinner.selectedItemPosition]
                val tencum = cum
                val city = listCity[i]

                when (cum) {
                    "BHD" -> cum = "bhd"
                    "CGV" -> cum = "cgv"
                    "Cinestar" -> cum = "cin"
                    "Dcine" -> cum = "dci"
                    "Galaxy" -> cum = "glx"
                    "Lotte" -> cum = "lot"
                    "Mega GS" -> cum = "meg"
                }

                list = if (cum == "Tất cả rạp") {
                    dbHelper.getCinemaALLbyCity(city)
                } else {
                    dbHelper.getCinemaALL(cum, city)
                }

                if (list.size == 0) {
                    recyclerView_cinema.visibility = GONE
                    text_noti.visibility = VISIBLE
                    text_noti.text = "Không tìm thấy rạp nào"
                } else {
                    recyclerView_cinema.visibility = VISIBLE
                    text_noti.visibility = GONE

                    list.sortedWith(compareBy { it.tenrap })
                    adapterCinema.clear()
                    for (m in list) {
                        adapterCinema.add(CinemaItem(m))
                    }
                    recyclerView_cinema.adapter = adapterCinema
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        spinner2.adapter = adapter2
        spinner2.setSelection(2)

        adapterCinema.setOnItemClickListener { item, _ ->
            val myItem = item as CinemaItem
            val intent = Intent(this, DetailCinemaActivity::class.java)
            intent.putExtra("tenrap", myItem.cinema.tenrap)
            startActivity(intent)
        }
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

class CinemaItem(val cinema: Cinema) : Item<ViewHolder>() {
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
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
