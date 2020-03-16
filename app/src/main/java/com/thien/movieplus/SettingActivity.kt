package com.thien.movieplus

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

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
