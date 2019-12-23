package com.thien.movieplus

import android.os.Bundle
import android.util.Log
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

    private var listCinema = ArrayList<Cinema>()
    private val adapterCinema = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cinema)

        supportActionBar?.title = "Rạp chiếu phim"
        copyDB()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView_cinema.layoutManager = layoutManager

        val dbHelper = DBHelper(this)
        listCinema = dbHelper.allCinema as ArrayList<Cinema>

        for (c in listCinema) {
            adapterCinema.add(CinemaItem(c))
        }

        recyclerView_cinema.adapter = adapterCinema
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

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            viewHolder.itemView.cine_name.text = cinema.tenrap
            viewHolder.itemView.cine_address.text = cinema.diachi

            when (cinema.cumrap) {
                "glx" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.glx)
                "cgv" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.cgv)
                "bhd" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.bhd)
                "lot" -> viewHolder.itemView.cine_image.setImageResource(R.drawable.lot)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
