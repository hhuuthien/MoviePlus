package com.thien.movieplus

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail_cinema.*

class DetailCinemaActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_cinema)

        val dbHelper = DBHelper(this)
        val cinema = dbHelper.getCinemabyName(intent.getStringExtra("tenrap")!!)

        deci_name.text = cinema.tenrap
        deci_address.text = cinema.diachi + ", " + cinema.quan + ", " + cinema.thanhpho

        when (cinema.cumrap) {
            "glx" -> deci_image.setImageResource(R.drawable.glx)
            "cgv" -> deci_image.setImageResource(R.drawable.cgv)
            "bhd" -> deci_image.setImageResource(R.drawable.bhd)
            "lot" -> deci_image.setImageResource(R.drawable.lot)
            "dci" -> deci_image.setImageResource(R.drawable.dci)
            "cin" -> deci_image.setImageResource(R.drawable.cin)
            "meg" -> deci_image.setImageResource(R.drawable.meg)
        }
    }
}
