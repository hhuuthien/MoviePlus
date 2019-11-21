package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_cast.*

class DetailCastActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bottom_cast_nav_info -> dc_view_pager.currentItem = 0
            R.id.bottom_cast_nav_movie -> dc_view_pager.currentItem = 1
            R.id.bottom_cast_nav_show -> dc_view_pager.currentItem = 2
            R.id.bottom_cast_nav_image -> dc_view_pager.currentItem = 3
        }
        return true
    }

    var castID: Int = -1
    private var posterPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_cast)

        castID = intent.getIntExtra("CAST_ID", -1)
        if (castID == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            //set Name and Title
            val castPoster = intent.getStringExtra("CAST_POSTER")
            if (castPoster == null || castPoster.isEmpty()) {
                dc_poster.setImageResource(R.drawable.logo_blue)
            } else {
                posterPath = castPoster
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300$castPoster")
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(dc_poster)
            }

            val castName = intent.getStringExtra("CAST_NAME")
            if (castName != null && castName.isNotEmpty()) {
                dc_name.text = castName
            }
        }

        dc_navigation.setOnNavigationItemSelectedListener(this)

        val bundle = Bundle()
        bundle.putInt("c_id", castID)

        val fdcinfo = FragmentDCInfo()
        fdcinfo.arguments = bundle
        val fdcmovie = FragmentDCMovie()
        fdcmovie.arguments = bundle
        val fdcshow = FragmentDCShow()
        fdcshow.arguments = bundle
        val fdcimage = FragmentDCImage()
        fdcimage.arguments = bundle

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(fdcinfo)
        adapter.addFrag(fdcmovie)
        adapter.addFrag(fdcshow)
        adapter.addFrag(fdcimage)
        dc_view_pager.adapter = adapter

        dc_view_pager.currentItem = 0
        dc_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> dc_navigation.selectedItemId = R.id.bottom_cast_nav_info
                    1 -> dc_navigation.selectedItemId = R.id.bottom_cast_nav_movie
                    2 -> dc_navigation.selectedItemId = R.id.bottom_cast_nav_show
                    3 -> dc_navigation.selectedItemId = R.id.bottom_cast_nav_image
                }
            }
        })

        dc_name.setOnClickListener {
            Toast.makeText(this, "ID: $castID", Toast.LENGTH_LONG).show()
        }

        dc_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}