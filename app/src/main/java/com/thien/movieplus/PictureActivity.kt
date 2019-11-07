package com.thien.movieplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_movie.*
import kotlinx.android.synthetic.main.activity_picture.*

class PictureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val path = intent.getStringExtra("imageString")

        Picasso.get()
            .load("https://image.tmdb.org/t/p/original$path")
            .placeholder(R.drawable.logo_accent)
            .into(picture_frame)
    }
}
