package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picture.*
import kotlinx.android.synthetic.main.download_layout.view.*

class PictureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val path = intent.getStringExtra("imageString")

        Picasso.get()
            .load("https://image.tmdb.org/t/p/original$path")
            .placeholder(R.drawable.logo_accent)
            .into(picture_frame)

        more.setOnClickListener {
            val myLayout = layoutInflater.inflate(R.layout.download_layout, null)
            val dialog = AlertDialog.Builder(this)
                .setView(myLayout)
                .create()
            myLayout.dummytext.setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this, PermissionActivity::class.java)
                    .putExtra("type", "toDownloadImage")
                    .putExtra("imageString", intent.getStringExtra("imageString"))
                startActivity(intent)
            }
            dialog.show()
        }
    }
}
