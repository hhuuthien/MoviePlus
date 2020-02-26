package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_detail_cinema.*

class DetailCinemaActivity : AppCompatActivity() {

    private var mapViewCine: MapView? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(
            this,
            "pk.eyJ1IjoiaGh1dXRoaWVuIiwiYSI6ImNrNzIxbTMwMjBiajczZm1sanNlcjNraWQifQ.MhsGYcW5jEtzImmZfIG6LA"
        )
        setContentView(R.layout.activity_detail_cinema)
        supportActionBar?.title = "Rạp chiếu phim"

        val dbHelper = DBHelper(this)
        val cinema = dbHelper.getCinemabyName(intent.getStringExtra("tenrap")!!)

        mapViewCine = findViewById(R.id.mapViewCine)
        mapViewCine?.onCreate(savedInstanceState)
        mapViewCine?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS)
            if (cinema.lat != null && cinema.lng != null) {
                val position = CameraPosition.Builder()
                    .target(LatLng(cinema.lat!!, cinema.lng!!))
                    .zoom(15.0)
                    .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
                mapboxMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(cinema.lat!!, cinema.lng!!))
                        .title(cinema.tenrap)
                )
            }
        }

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
}
