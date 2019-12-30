package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
        deci_info.text = cinema.gioithieu
        deci_showtime.text =
            "Ứng dụng chưa hỗ trợ xem lịch chiếu của rạp này. Vui lòng truy cập website chính thức của rạp:"

        when (cinema.cumrap) {
            "glx" -> {
                deci_image.setImageResource(R.drawable.glx)
                deci_link.text = "https://www.galaxycine.vn"
            }
            "cgv" -> {
                deci_image.setImageResource(R.drawable.cgv)
                deci_link.text = "https://www.cgv.vn"
            }
            "bhd" -> {
                deci_image.setImageResource(R.drawable.bhd)
                deci_link.text = "https://www.bhdstar.vn"
            }
            "lot" -> {
                deci_image.setImageResource(R.drawable.lot)
                deci_link.text = "http://www.lottecinemavn.com"
            }
            "dci" -> {
                deci_image.setImageResource(R.drawable.dci)
                deci_link.text = "https://www.dcine.vn"
            }
            "cin" -> {
                deci_image.setImageResource(R.drawable.cin)
                deci_link.text = "https://cinestar.com.vn"
            }
            "meg" -> {
                deci_image.setImageResource(R.drawable.meg)
                deci_link.text = "https://www.megagscinemas.vn"
            }
        }

        deci_link.setOnClickListener {
            val link = deci_link.text.toString()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            this.startActivity(intent)
        }
    }
}
