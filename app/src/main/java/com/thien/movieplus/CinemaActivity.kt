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
        listCum.add("Khác")

        val listCity = ArrayList<String>()
        listCity.add("Cả nước")
        listCity.add("An Giang")
        listCity.add("Bà Rịa - Vũng Tàu")
        listCity.add("Bắc Giang")
        listCity.add("Bắc Ninh")
        listCity.add("Bình Dương")
        listCity.add("Bình Thuận")
        listCity.add("Bình Định")
        listCity.add("Bến Tre")
        listCity.add("Cà Mau")
        listCity.add("Cần Thơ")
        listCity.add("Đà Nẵng")
        listCity.add("Đắk Lắk")
        listCity.add("Đồng Nai")
        listCity.add("Đồng Tháp")
        listCity.add("Hà Nam")
        listCity.add("Hà Nội")
        listCity.add("Hà Tĩnh")
        listCity.add("Hải Dương")
        listCity.add("Hải Phòng")
        listCity.add("Hậu Giang")
        listCity.add("Hồ Chí Minh")
        listCity.add("Khánh Hoà")
        listCity.add("Kiên Giang")
        listCity.add("Kon Tum")
        listCity.add("Lạng Sơn")
        listCity.add("Lâm Đồng")
        listCity.add("Nam Định")
        listCity.add("Nghệ An")
        listCity.add("Ninh Bình")
        listCity.add("Ninh Thuận")
        listCity.add("Phú Thọ")
        listCity.add("Phú Yên")
        listCity.add("Quảng Bình")
        listCity.add("Quảng Nam")
        listCity.add("Quảng Ngãi")
        listCity.add("Quảng Ninh")
        listCity.add("Sóc Trăng")
        listCity.add("Sơn La")
        listCity.add("Thanh Hoá")
        listCity.add("Thái Bình")
        listCity.add("Thái Nguyên")
        listCity.add("Thừa Thiên - Huế")
        listCity.add("Tiền Giang")
        listCity.add("Trà Vinh")
        listCity.add("Tây Ninh")
        listCity.add("Tuyên Quang")
        listCity.add("Vĩnh Long")
        listCity.add("Yên Bái")

        val adapter = ArrayAdapter(this, R.layout.spinner_item2, listCum)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item2, listCity)
        adapter2.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)

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
                    "Khác" -> cum = "abc"
                }

                if (cum == "Tất cả rạp") {
                    if (city == "Cả nước") {
                        list = dbHelper.allCinema as ArrayList<Cinema>
                        texttext.text = "Có ${list.size} rạp khắp cả nước"
                    } else {
                        list = dbHelper.getCinemaALLbyCity(city)
                        texttext.text = "Có ${list.size} rạp ở $city"
                    }
                } else {
                    if (city == "Cả nước") {
                        list = dbHelper.getCinemaALLbyCum(cum)
                        texttext.text = "Có ${list.size} rạp $tencum khắp cả nước"
                    } else {
                        list = dbHelper.getCinemaALL(cum, city)
                        texttext.text = "Có ${list.size} rạp $tencum ở $city"
                    }
                }

                list.sortedWith(compareBy { it.tenrap })
                adapterCinema.clear()
                for (m in list) {
                    adapterCinema.add(CinemaItem(m))
                }
                recyclerView_cinema.adapter = adapterCinema
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(2)


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
                    "Khác" -> cum = "abc"
                }

                if (cum == "Tất cả rạp") {
                    if (city == "Cả nước") {
                        list = dbHelper.allCinema as ArrayList<Cinema>
                        texttext.text = "Có ${list.size} rạp khắp cả nước"
                    } else {
                        list = dbHelper.getCinemaALLbyCity(city)
                        texttext.text = "Có ${list.size} rạp ở $city"
                    }
                } else {
                    if (city == "Cả nước") {
                        list = dbHelper.getCinemaALLbyCum(cum)
                        texttext.text = "Có ${list.size} rạp $tencum khắp cả nước"
                    } else {
                        list = dbHelper.getCinemaALL(cum, city)
                        texttext.text = "Có ${list.size} rạp $tencum ở $city"
                    }
                }

                list.sortedWith(compareBy { it.tenrap })
                adapterCinema.clear()
                for (m in list) {
                    adapterCinema.add(CinemaItem(m))
                }
                recyclerView_cinema.adapter = adapterCinema
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
        spinner2.adapter = adapter2
        spinner2.setSelection(21)
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
