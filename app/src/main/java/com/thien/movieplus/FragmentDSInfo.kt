package com.thien.movieplus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_ds_info.*
import kotlinx.android.synthetic.main.text_layout.view.*
import okhttp3.*
import java.io.IOException

class FragmentDSInfo : Fragment() {

    companion object {
        var overview: String = ""
        var website: String = ""
    }

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

        view.findViewById<TextView>(R.id.ds_overview).setOnClickListener {
            val myLayout = layoutInflater.inflate(R.layout.text_layout, null)
            myLayout.textview.text = overview
            val dialog = AlertDialog.Builder(context!!)
                .setView(myLayout)
                .create()
            dialog.show()
        }

        view.findViewById<TextView>(R.id.ds_web).setOnClickListener {
            if (website.isEmpty() || website.isBlank() || website == "" || website == "Đang cập nhật") {
                return@setOnClickListener
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Không thể truy cập địa chỉ này", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun fetch(showId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.ds_loading_5).visibility = VISIBLE
        view.findViewById<RelativeLayout>(R.id.ds_info_layout_child).visibility = GONE

        val url =
            "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailShow = gSon.fromJson(body, DeShow::class.java)

                overview = if (detailShow.overview == null || detailShow.overview.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailShow.overview
                }

                website = if (detailShow.homepage == null || detailShow.homepage.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailShow.homepage
                }

                activity?.runOnUiThread {
                    try {
                        ds_overview.text = overview
                        ds_web.text = website
                        ds_numEp.text = detailShow.number_of_episodes.toString()

                        val day = detailShow.first_air_date.substring(8, 10)
                        val month = detailShow.first_air_date.substring(5, 7)
                        val year = detailShow.first_air_date.substring(0, 4)
                        ds_date.text = "$day-$month-$year"

                        if (detailShow.last_episode_to_air != null) {
                            val dayLast = detailShow.last_episode_to_air.air_date?.substring(8, 10)
                            val monthLast = detailShow.last_episode_to_air.air_date?.substring(5, 7)
                            val yearLast = detailShow.last_episode_to_air.air_date?.substring(0, 4)

                            ds_last_1.text =
                                "Tập ${detailShow.last_episode_to_air.episode_number} - Mùa ${detailShow.last_episode_to_air.season_number}:  ${detailShow.last_episode_to_air.name}"
                            ds_last_2.text = "$dayLast-$monthLast-$yearLast"
                        }

                        if (detailShow.next_episode_to_air != null) {
                            val dayNext = detailShow.next_episode_to_air.air_date?.substring(8, 10)
                            val monthNext = detailShow.next_episode_to_air.air_date?.substring(5, 7)
                            val yearNext = detailShow.next_episode_to_air.air_date?.substring(0, 4)

                            ds_next_1.text =
                                "Tập ${detailShow.next_episode_to_air.episode_number} - Mùa ${detailShow.next_episode_to_air.season_number}:  ${detailShow.next_episode_to_air.name}"
                            ds_next_2.text = "$dayNext-$monthNext-$yearNext"
                        }

                        val company = detailShow.production_companies
                        if (company == null || company.size == 0) {
                            ds_company.text = "Đang cập nhật"
                        } else if (company.size == 1) {
                            ds_company.text = company[0].name
                        } else {
                            val size = company.size
                            for (i in 0 until size - 1) {
                                ds_company.append(company[i].name + "\n")
                            }
                            ds_company.append(company[size - 1].name)
                        }
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }

                    view.findViewById<ProgressBar>(R.id.ds_loading_5).visibility = GONE
                    view.findViewById<RelativeLayout>(R.id.ds_info_layout_child).visibility =
                        VISIBLE
                }
            }
        })
    }
}