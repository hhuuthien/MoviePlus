package com.thien.movieplus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_dc_info.*
import okhttp3.*
import java.io.IOException

class FragmentDCInfo : Fragment() {

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
    }

    private fun fetch(castId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dc_loading_1).visibility = VISIBLE
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

                activity?.runOnUiThread {
                    if (detailCast.biography == null || detailCast.biography.isEmpty()) {
                        dc_bio.text = "Biography: ${detailCast.name}"
                    } else {
                        dc_bio.text = detailCast.biography
                    }

                    if (detailCast.birthday == null) {
                        dc_birthday.text = "Ngày sinh: đang cập nhật"
                    } else {
                        val day = detailCast.birthday.substring(8, 10)
                        val month = detailCast.birthday.substring(5, 7)
                        val year = detailCast.birthday.substring(0, 4)
                        dc_birthday.text = "Ngày sinh: $day-$month-$year"
                    }

                    if (detailCast.place_of_birth == null) {
                        dc_birthplace.text = "Quê quán: đang cập nhật"
                    } else {
                        dc_birthplace.text = "Quê quán: ${detailCast.place_of_birth}"
                    }

                    view.findViewById<ProgressBar>(R.id.dc_loading_1).visibility = GONE
                }
            }
        })
    }
}

class DetailCast(
    val birthday: String?,
    val id: Int,
    val name: String,
    val biography: String?,
    val place_of_birth: String?,
    val profile_path: String?,
    val movies: ArrayList<Movie>
)
