package com.thien.movieplus

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.ep_item.view.*
import kotlinx.android.synthetic.main.fragment_ds_season.*
import kotlinx.android.synthetic.main.season_layout.view.*
import okhttp3.*
import java.io.IOException

class FragmentDSSeason : Fragment() {

    private var listS = ArrayList<Season>()
    private val adapterS = GroupAdapter<ViewHolder>()
    private var showName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ds_season, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val pref = context!!.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetchSeason(showId.toString(), view, goodquality)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.ds_list_season).layoutManager = layoutManager

        adapterS.setOnItemClickListener { item, _ ->
            val myItem = item as SeasonItem

            val myLayoutInflater = layoutInflater.inflate(R.layout.season_layout, null)
            myLayoutInflater.dss_title.text = myItem.season.name
            myLayoutInflater.dss_title2.text = showName
            myLayoutInflater.dss_list_ep.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            Picasso.get().load("https://image.tmdb.org/t/p/w300/${myItem.season.poster_path}")
                .fit()
                .into(myLayoutInflater.dss_poster)

            val url =
                "https://api.themoviedb.org/3/tv/$showId/season/${myItem.season.season_number}?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("onFetchingResult", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, EpList::class.java)

                    val listEp = result.episodes
                    val adapterEp = GroupAdapter<ViewHolder>()

                    activity?.runOnUiThread {
                        try {
                            for (m in listEp) {
                                adapterEp.add(EpItem(m))
                            }
                            myLayoutInflater.dss_list_ep.adapter = adapterEp

                            val dialog = AlertDialog.Builder(context)
                                .setView(myLayoutInflater).create()
                            dialog.show()
                        } catch (e: Exception) {
                            Log.d("error_here", e.toString())
                        }
                    }
                }
            })
        }
    }

    private fun fetchSeason(showId: String, view: View, goodquality: Boolean) {
        view.findViewById<ProgressBar>(R.id.ds_loading_9).visibility = View.VISIBLE

        val url =
            "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283"
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

                showName = detailShow.name.toString()

                activity?.runOnUiThread {
                    try {
                        listS.clear()
                        adapterS.clear()
                        listS = detailShow.seasons

                        for (m in listS) {
                            if (m.season_number != 0) {
                                adapterS.add(SeasonItem(m, goodquality))
                            }
                        }
                        ds_list_season.adapter = adapterS

                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }

                    view.findViewById<ProgressBar>(R.id.ds_loading_9).visibility = View.GONE
                }
            }
        })
    }
}

class Ep(
    val name: String?,
    val air_date: String?,
    val episode_number: Int?,
    val overview: String?,
    val id: Int?,
    val season_number: Int?
)

class EpList(
    val episodes: ArrayList<Ep>
)

class EpItem(val ep: Ep) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.ep_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            viewHolder.itemView.ep_title.text = ep.name
            viewHolder.itemView.ep_num.text = ep.episode_number.toString()
            if (ep.overview == null || ep.overview == "") {
                viewHolder.itemView.ep_overview.visibility = View.GONE
            } else {
                viewHolder.itemView.ep_overview.text = ep.overview
            }

            val day = ep.air_date?.substring(8, 10)
            val month = ep.air_date?.substring(5, 7)
            val year = ep.air_date?.substring(0, 4)
            viewHolder.itemView.ep_date.text = "$day-$month-$year"
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}