package com.thien.movieplus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_dm_info.*
import okhttp3.*
import java.io.IOException

class FragmentDMInfo : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_info, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val movieId = arguments?.getInt("m_id", -1)
        if (movieId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(movieId.toString(),view)
        }
    }

    private fun fetch(movieId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = VISIBLE

        val url =
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovie::class.java)

                activity?.runOnUiThread {
                    if (detailMovie.overview == null || detailMovie.overview.isEmpty()) {
                        dm_overview.text = ""
                    } else {
                        dm_overview.text = detailMovie.overview
                    }
                    view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = GONE
                }
            }
        })
    }
}

class DetailMovie(
    val backdrop_path: String?,
    val poster_path: String?,
    val title: String,
    val overview: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val release_date: String?,
    val runtime: Int?,
    val genres: ArrayList<Genre>,
    val original_language: String?,
    val tagline: String?
)

class Genre(
    val id:Int,
    val name: String?
)
