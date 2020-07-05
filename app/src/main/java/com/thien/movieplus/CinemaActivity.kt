package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_cinema.*
import kotlinx.android.synthetic.main.cinema_item.view.*
import kotlinx.android.synthetic.main.dialog_filter_cinema.view.*
import java.io.File
import java.io.FileOutputStream

class CinemaActivity : AppCompatActivity() {

    private var list = ArrayList<Cinema>()
    private val adapterCinema = GroupAdapter<ViewHolder>()
    private var numCum = 2
    private var numCity = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cinema)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = "Rạp chiếu phim"

        copyDB()
        val dbHelper = DBHelper(this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_cinema.layoutManager = layoutManager

        list.clear()
        list = dbHelper.getCinemaALL("cgv", "Hồ Chí Minh")
        adapterCinema.clear()
        for (m in list) {
            adapterCinema.add(CinemaItem(m))
        }
        recyclerView_cinema.adapter = adapterCinema
        text_number.text = "Đang hiển thị ${list.size} rạp CGV ở Hồ Chí Minh"

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_cinema, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> {
                startActivity(Intent(this, FindCinemaActivity::class.java))
            }
            R.id.menu_filter -> {
                val inflater = layoutInflater.inflate(R.layout.dialog_filter_cinema, null)
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setView(inflater)
                dialogBuilder.setCancelable(true)
                val dialog = dialogBuilder.create()

                val listCum = ArrayList<String>()
                listCum.add("Tất cả rạp")
                listCum.add("BHD")
                listCum.add("CGV")
                listCum.add("Cinestar")
                listCum.add("Dcine")
                listCum.add("Galaxy")
                listCum.add("Lotte")
                listCum.add("Mega GS")
                val adapterA = ArrayAdapter(this, R.layout.spinner_item, listCum)
                adapterA.setDropDownViewResource(R.layout.spinner_item_choice)
                val spinnerA = inflater.spinner_a
                spinnerA.adapter = adapterA
                spinnerA.setSelection(numCum)

                val listCity = ArrayList<String>()
                listCity.add("Bến Tre")
                listCity.add("Bình Dương")
                listCity.add("Hồ Chí Minh")
                listCity.add("Tiền Giang")
                val adapterB = ArrayAdapter(this, R.layout.spinner_item, listCity)
                adapterB.setDropDownViewResource(R.layout.spinner_item_choice)
                val spinnerB = inflater.spinner_b
                spinnerB.adapter = adapterB
                spinnerB.setSelection(numCity)

                val buttonApply = inflater.button_apply
                buttonApply.setOnClickListener {
                    dialog.dismiss()
                    numCum = spinnerA.selectedItemPosition
                    var cum = listCum[numCum]
                    val tenCum = cum
                    numCity = spinnerB.selectedItemPosition
                    val city = listCity[numCity]
                    when (cum) {
                        "BHD" -> cum = "bhd"
                        "CGV" -> cum = "cgv"
                        "Cinestar" -> cum = "cin"
                        "Dcine" -> cum = "dci"
                        "Galaxy" -> cum = "glx"
                        "Lotte" -> cum = "lot"
                        "Mega GS" -> cum = "meg"
                    }
                    val dbHelper = DBHelper(this)
                    list.clear()
                    if (cum == "Tất cả rạp") {
                        list = dbHelper.getCinemaALLbyCity(city)
                        text_number.text = "Đang hiển thị tất cả ${list.size} rạp ở $city"
                    } else {
                        list = dbHelper.getCinemaALL(cum, city)
                        if (list.size == 0) {
                            text_number.text = "Không tìm thấy rạp $tenCum nào ở $city"
                        } else {
                            text_number.text = "Đang hiển thị ${list.size} rạp $tenCum ở $city"
                        }
                    }
                    adapterCinema.clear()
                    for (m in list) {
                        adapterCinema.add(CinemaItem(m))
                    }
                    recyclerView_cinema.adapter = adapterCinema
                }

                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
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