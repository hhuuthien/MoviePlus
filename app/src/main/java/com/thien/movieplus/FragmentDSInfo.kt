package com.thien.movieplus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_ds_info.*
import okhttp3.*
import java.io.IOException

class FragmentDSInfo : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ds_info, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(showId.toString(), view)
        }
    }

    private fun fetch(showId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.ds_loading_1).visibility = View.VISIBLE

        val url =
            "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.ds_loading_1).visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailShow = gSon.fromJson(body, DeShow::class.java)

                activity?.runOnUiThread {
                    try {
                        if (detailShow.overview == null || detailShow.overview.isEmpty()) {
                            ds_overview.text = "Overview: ${detailShow.name}"
                        } else {
                            ds_overview.text = detailShow.overview
                        }

                        val day = detailShow.first_air_date.substring(8, 10)
                        val month = detailShow.first_air_date.substring(5, 7)
                        val year = detailShow.first_air_date.substring(0, 4)
                        ds_date.text = "Ngày bắt đầu: $day-$month-$year"

                        ds_numEp.text = "Số tập: ${detailShow.number_of_episodes}"

                        if (detailShow.last_episode_to_air != null) {
                            val dayLast = detailShow.last_episode_to_air.air_date?.substring(8, 10)
                            val monthLast = detailShow.last_episode_to_air.air_date?.substring(5, 7)
                            val yearLast = detailShow.last_episode_to_air.air_date?.substring(0, 4)

                            ds_last_date.text = "\t • Ngày chiếu: $dayLast-$monthLast-$yearLast"
                            ds_last_text.text =
                                "Tập gần đây nhất: Tập ${detailShow.last_episode_to_air.episode_number} - Mùa ${detailShow.last_episode_to_air.season_number}"
                            ds_last_name.text = "\t • ${detailShow.last_episode_to_air.name}"
                        }

                        if (detailShow.next_episode_to_air != null) {
                            val dayNext = detailShow.next_episode_to_air.air_date?.substring(8, 10)
                            val monthNext = detailShow.next_episode_to_air.air_date?.substring(5, 7)
                            val yearNext = detailShow.next_episode_to_air.air_date?.substring(0, 4)

                            ds_next_date.text = "\t • Ngày chiếu: $dayNext-$monthNext-$yearNext"
                            ds_next_text.text =
                                "Tập tiếp theo: Tập ${detailShow.next_episode_to_air.episode_number} - Mùa ${detailShow.next_episode_to_air.season_number}"
                            ds_next_name.text = "\t • ${detailShow.next_episode_to_air.name}"
                        }
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }

                    view.findViewById<ProgressBar>(R.id.ds_loading_1).visibility = GONE
                }
            }
        })
    }
}