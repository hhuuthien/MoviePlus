package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_picture.*

class PictureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        setSupportActionBar(m_toolbar)

        val path = intent.getStringExtra("imageString")
        if (path != null) {
            Picasso.get()
                .load("https://image.tmdb.org/t/p/original$path")
                .into(picture_frame, object : Callback {
                    @SuppressLint("SetTextI18n")
                    override fun onSuccess() {
                        picture_loading.visibility = GONE
                        val w = picture_frame.drawable.intrinsicWidth
                        val h = picture_frame.drawable.intrinsicHeight
                        picture_size.text = "Kích thước: $w x $h"
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(this@PictureActivity, "Đã xảy ra lỗi", Toast.LENGTH_LONG)
                            .show()
                    }
                })
        } else {
            picture_loading.visibility = GONE
            picture_size.visibility = GONE
            Picasso.get()
                .load(intent.getStringExtra("avatarString"))
                .into(picture_frame)
        }
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