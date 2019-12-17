package com.thien.movieplus

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        supportActionBar?.title = "Thông tin ứng dụng"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_refresh, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                val dialog = AlertDialog.Builder(this)
                    .setMessage("Ứng dụng sẽ đóng lại sau khi cập nhật dữ liệu mới.")
                    .setPositiveButton("Cập nhật") { _, _ ->
                        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }
                    .setNegativeButton("Huỷ bỏ") { _, _ ->

                    }
                    .setCancelable(true)
                    .create()
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
