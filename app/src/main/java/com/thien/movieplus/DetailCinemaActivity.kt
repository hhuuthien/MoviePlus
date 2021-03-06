package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail_cinema.*

class DetailCinemaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var lat = 0.0
    private var lng = 0.0
    private var name = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_cinema)

        //maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = "Chi tiết rạp chiếu phim"

        val dbHelper = DBHelper(this)
        val cinema = dbHelper.getCinemabyName(intent.getStringExtra("tenrap")!!)

        name = cinema.tenrap
        deci_name.text = name
        deci_address.text = cinema.diachi + ", " + cinema.quan + ", " + cinema.thanhpho
        deci_info.text = cinema.gioithieu
        if (cinema.lat != null) {
            lat = cinema.lat!!
        }
        if (cinema.lng != null) {
            lng = cinema.lng!!
        }

        when (cinema.cumrap) {
            "glx" -> deci_image.setImageResource(R.drawable.glx)
            "cgv" -> deci_image.setImageResource(R.drawable.cgv)
            "bhd" -> deci_image.setImageResource(R.drawable.bhd)
            "lot" -> deci_image.setImageResource(R.drawable.lot)
            "dci" -> deci_image.setImageResource(R.drawable.dci)
            "cin" -> deci_image.setImageResource(R.drawable.cin)
            "meg" -> deci_image.setImageResource(R.drawable.meg)
        }

        deci_access.setOnClickListener {
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val cinemaLatLng = LatLng(lat, lng)
        mMap.addMarker(MarkerOptions().position(cinemaLatLng).title(name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cinemaLatLng, 17F))
    }
}