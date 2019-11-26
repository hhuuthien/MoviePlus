package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
import kotlinx.android.synthetic.main.fragment_ds_season.*
import kotlinx.android.synthetic.main.season_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDSSeason : Fragment() {

    private var list = ArrayList<Season>()
    private val adapter = GroupAdapter<ViewHolder>()

    companion object {
        private var showName = ""
    }

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
        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(showId.toString(), view)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.ds_list_season).layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            val myItem = item as SeasonItem
            startActivity(
                Intent(context, DetailSeasonActivity::class.java)
                    .putExtra("showID", showId)
                    .putExtra("showName", showName)
                    .putExtra("seasonNumber", myItem.season.season_number)
                    .putExtra("seasonName", myItem.season.name)
                    .putExtra("seasonDate", myItem.season.air_date)
                    .putExtra("seasonImage", myItem.season.poster_path)
            )
        }
    }

    private fun fetch(showId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.ds_loading_8).visibility = VISIBLE

        val url =
            "https://api.themoviedb.org/3/tv/${showId}?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
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

                showName = detailShow.name

                list.clear()
                list = detailShow.seasons

                activity?.runOnUiThread {
                    adapter.clear()
                    for (m in list) {
                        adapter.add(SeasonItem(m))
                    }
                    ds_list_season.adapter = adapter

                    view.findViewById<ProgressBar>(R.id.ds_loading_8).visibility = GONE
                }
            }
        })
    }
}

class SeasonItem(val season: Season) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.season_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (season.poster_path == null) {
                viewHolder.itemView.ss_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + season.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.ss_poster)
            }

            viewHolder.itemView.ss_title.text = season.name
            viewHolder.itemView.ss_number.text = "Mùa ${season.season_number}"
            viewHolder.itemView.ss_num_ep.text = "Số tập: ${season.episode_count}"

            val day = season.air_date?.substring(8, 10)
            val month = season.air_date?.substring(5, 7)
            val year = season.air_date?.substring(0, 4)
            viewHolder.itemView.ss_date.text = "$day-$month-$year"
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}