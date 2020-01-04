package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
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
        deci_info.text = cinema.gioithieu

        when (cinema.cumrap) {
            "glx" -> deci_image.setImageResource(R.drawable.glx)
            "cgv" -> deci_image.setImageResource(R.drawable.cgv)
            "bhd" -> deci_image.setImageResource(R.drawable.bhd)
            "lot" -> deci_image.setImageResource(R.drawable.lot)
            "dci" -> deci_image.setImageResource(R.drawable.dci)
            "cin" -> deci_image.setImageResource(R.drawable.cin)
            "meg" -> deci_image.setImageResource(R.drawable.meg)
        }

        val snackbar = Snackbar.make(
            cinema_view,
            "Ứng dụng chưa hỗ trợ xem lịch chiếu của rạp này.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Đi đến website") {
            var link = ""
            when (cinema.cumrap) {
                "glx" -> link = "https://www.galaxycine.vn"
                "cgv" -> link = "https://www.cgv.vn"
                "bhd" -> link = "https://www.bhdstar.vn"
                "lot" -> link = "http://www.lottecinemavn.com"
                "dci" -> link = "https://www.dcine.vn"
                "cin" -> link = "https://cinestar.com.vn"
                "meg" -> link = "https://www.megagscinemas.vn"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        }
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.show()
    }
}
