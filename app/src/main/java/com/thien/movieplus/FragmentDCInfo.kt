package com.thien.movieplus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_dc_info.*
import kotlinx.android.synthetic.main.text_layout.view.*
import okhttp3.*
import java.io.IOException

class FragmentDCInfo : Fragment() {

    companion object {
        var bio: String = ""
        var isDead = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dc_info, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val castId = arguments?.getInt("c_id", -1)
        if (castId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(castId.toString(), view)
        }

        view.findViewById<TextView>(R.id.dc_overview).setOnClickListener {
            val myLayout = layoutInflater.inflate(R.layout.text_layout, null)
            myLayout.textview.text = bio
            val dialog = AlertDialog.Builder(context!!)
                .setView(myLayout)
                .create()
            dialog.show()
        }
    }

    private fun fetch(castId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dc_loading_1).visibility = VISIBLE
        view.findViewById<RelativeLayout>(R.id.dc_info_layout_child).visibility = GONE

        val url =
            "https://api.themoviedb.org/3/person/$castId?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.dc_loading_1).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailCast = gSon.fromJson(body, DetailCast::class.java)

                bio = if (detailCast.biography == null || detailCast.biography.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailCast.biography
                }

                isDead = detailCast.deathday != null

                activity?.runOnUiThread {
                    dc_overview.text = bio

                    if (detailCast.birthday == null) {
                        dc_date.text = "Đang cập nhật"
                    } else {
                        val day = detailCast.birthday.substring(8, 10)
                        val month = detailCast.birthday.substring(5, 7)
                        val year = detailCast.birthday.substring(0, 4)
                        dc_date.text = "$day-$month-$year"
                    }

                    if (isDead) {
                        dc_date2_text.visibility = VISIBLE
                        dc_date2.visibility = VISIBLE

                        val day = detailCast.deathday?.substring(8, 10)
                        val month = detailCast.deathday?.substring(5, 7)
                        val year = detailCast.deathday?.substring(0, 4)
                        dc_date2.text = "$day-$month-$year"
                    } else {
                        dc_date2_text.visibility = GONE
                        dc_date2.visibility = GONE
                    }

                    if (detailCast.place_of_birth == null) {
                        dc_place.text = "Đang cập nhật"
                    } else {
                        dc_place.text = detailCast.place_of_birth
                    }

                    view.findViewById<ProgressBar>(R.id.dc_loading_1).visibility = GONE
                    view.findViewById<RelativeLayout>(R.id.dc_info_layout_child).visibility =
                        VISIBLE
                }
            }
        })
    }
}

class DetailCast(
    val birthday: String?,
    val deathday: String?,
    val id: Int,
    val name: String,
    val biography: String?,
    val place_of_birth: String?,
    val profile_path: String?,
    val movies: ArrayList<Movie>
)
