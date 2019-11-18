package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.picture_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_download -> {
                val intent = Intent(this, PermissionActivity::class.java)
                    .putExtra("type", "toDownloadImage")
                    .putExtra("imageString", intent.getStringExtra("imageString"))
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
