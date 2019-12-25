package com.thien.movieplus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_detail_season.*
import kotlinx.android.synthetic.main.ep_item.view.*
import okhttp3.*
import java.io.IOException

class DetailSeasonActivity : AppCompatActivity() {

    private var showID: Int = -1
    private var seasonNumber: Int = -1

    private var list = ArrayList<DeEp>()
    private val adapter = GroupAdapter<ViewHolder>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_season)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dss_list_eps.layoutManager = layoutManager

        val posterPath = intent.getStringExtra("seasonImage")
        dss_poster.setOnClickListener {
            if (posterPath != "") {
                val intent = Intent(this, PictureActivity::class.java)
                intent.putExtra("imageString", posterPath)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        showID = intent.getIntExtra("showID", -1)
        seasonNumber = intent.getIntExtra("seasonNumber", -1)
        if (showID == -1 || seasonNumber == -1) {
            Toast.makeText(applicationContext, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            dss_title.text = intent.getStringExtra("showName")
            dss_title_ss.text = intent.getStringExtra("seasonName")

            val date = intent.getStringExtra("seasonDate")
            val day = date?.substring(8, 10)
            val month = date?.substring(5, 7)
            val year = date?.substring(0, 4)
            dss_date.text = "$day-$month-$year"

            Picasso.get()
                .load("https://image.tmdb.org/t/p/w300$posterPath")
                .placeholder(R.drawable.logo_accent)
                .fit()
                .into(dss_poster)

            fetch(showID.toString(), seasonNumber.toString())
        }
    }

    private fun fetch(showID: String, seasonNumber: String) {
        dss_loading_1.visibility = VISIBLE
        val url =
            "https://api.themoviedb.org/3/tv/$showID/season/$seasonNumber?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val deSeason = gSon.fromJson(body, DeSeason::class.java)

                list.clear()
                list = deSeason.episodes

                runOnUiThread {
                    adapter.clear()
                    for (m in list) {
                        adapter.add(EpItem(m))
                    }
                    dss_list_eps.adapter = adapter
                    dss_loading_1.visibility = GONE
                }
            }
        })
    }
}

class DeSeason(
    val air_date: String?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int?,
    val episodes: ArrayList<DeEp>
)

class DeEp(
    val id: Int,
    val air_date: String?,
    val episode_number: Int?,
    val name: String?,
    val overview: String?,
    val season_number: Int?,
    val show_id: Int?
)

class EpItem(private val ep: DeEp) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.ep_item
    }

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.ep_name.text = ep.name

        val day = ep.air_date?.substring(8, 10)
        val month = ep.air_date?.substring(5, 7)
        val year = ep.air_date?.substring(0, 4)
        viewHolder.itemView.ep_date.text = "$day-$month-$year"
    }
}
