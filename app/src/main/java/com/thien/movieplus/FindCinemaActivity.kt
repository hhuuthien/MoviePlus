package com.thien.movieplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_find_cinema.*

class FindCinemaActivity : AppCompatActivity() {

    private var list = ArrayList<Cinema>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_cinema)
        supportActionBar?.title = "Tìm kiếm rạp chiếu phim"
        val dbHelper = DBHelper(this)

        list.clear()
        adapter.clear()

        find_noti.visibility = View.GONE
        find_list.visibility = View.GONE

        find_key.text = null
        find_key.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        find_key.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val key = p0.toString().trim()
                if (key.isNotEmpty()) {
                    list.clear()
                    adapter.clear()

                    list = dbHelper.findCinema(key)

                    if (list.size != 0) {
                        find_noti.visibility = View.GONE
                        find_list.visibility = View.VISIBLE
                    } else {
                        find_noti.visibility = View.VISIBLE
                        find_list.visibility = View.GONE
                    }

                    for (l in list) {
                        adapter.add(CinemaItem(l))
                    }
                    find_list.adapter = adapter
                } else {
                    list.clear()
                    adapter.clear()
                }
            }
        })

        adapter.setOnItemClickListener { item, _ ->
            val myItem = item as CinemaItem
            val intent = Intent(this, DetailCinemaActivity::class.java)
            intent.putExtra("tenrap", myItem.cinema.tenrap)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}
