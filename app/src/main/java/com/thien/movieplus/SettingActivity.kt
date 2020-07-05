package com.thien.movieplus

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(m_toolbar)
        supportActionBar?.title = "Cài đặt"

        val pref =
            applicationContext.getSharedPreferences("SettingPref", 0)
        val editor: SharedPreferences.Editor = pref.edit()

        switch_eng.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                editor.putBoolean("english", true)
                editor.apply()
            } else {
                editor.putBoolean("english", false)
                editor.apply()
            }
        }

        switch_size.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                editor.putBoolean("goodquality", true)
                editor.apply()
            } else {
                editor.putBoolean("goodquality", false)
                editor.apply()
            }
        }

        switch_default.setOnClickListener {
            switch_eng.isChecked = false
            switch_size.isChecked = true
            editor.putBoolean("english", false)
            editor.putBoolean("goodquality", true)
            editor.apply()
            Toast.makeText(this, "Đã khôi phục cài đặt mặc định", Toast.LENGTH_LONG).show()
        }

        switch_cache.setOnClickListener {
            try {
                Toast.makeText(this, "Đã xoá dữ liệu bộ nhớ đệm", Toast.LENGTH_LONG).show()
                applicationContext.cacheDir.deleteRecursively()
            } catch (e: Exception) {
                Toast.makeText(this, "Đã xảy ra lỗi. Vui lòng thử lại sau", Toast.LENGTH_LONG)
                    .show()
            }
        }

        switch_setting.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val pref =
            applicationContext.getSharedPreferences("SettingPref", 0)

        val english = pref.getBoolean("english", false)
        switch_eng.isChecked = english
        val goodquality = pref.getBoolean("goodquality", true)
        switch_size.isChecked = goodquality
    }
}
