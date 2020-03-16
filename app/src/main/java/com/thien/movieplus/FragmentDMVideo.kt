package com.thien.movieplus

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import kotlinx.android.synthetic.main.fragment_dm_video.*
import kotlinx.android.synthetic.main.video_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDMVideo : Fragment() {

    private val adapterV = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_video, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val movieId = arguments?.getInt("m_id", -1)
        if (movieId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetchTrailer(movieId.toString(), view)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.dm_list_video).layoutManager = layoutManager

        adapterV.setOnItemClickListener { item, _ ->
            val myItem = item as VideoItem
            val video = myItem.video
            val appIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${video.key}"))
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=${video.key}")
            )
            try {
                startActivity(appIntent)
            } catch (ex: ActivityNotFoundException) {
                startActivity(webIntent)
            }
        }
    }

    private fun fetchTrailer(movieId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_9).visibility = View.VISIBLE
        val listV = ArrayList<Video>()

        val url1 =
            "https://api.themoviedb.org/3/movie/$movieId/videos?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request1 = Request.Builder().url(url1).build()
        val client1 = OkHttpClient()
        client1.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, VideoResult::class.java)

                val listTemp1 = result.results
                for (video in listTemp1) {
                    if (video.site == "YouTube") {
                        listV.add(video)
                    }
                }

                fetchTrailer2(listV, movieId, view)
            }
        })
    }

    private fun fetchTrailer2(listV: ArrayList<Video>, movieId: String, view: View) {
        val url2 =
            "https://api.themoviedb.org/3/movie/$movieId/videos?api_key=d4a7514dbdd976453d2679e036009283"
        val request2 = Request.Builder().url(url2).build()
        val client2 = OkHttpClient()
        client2.newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, VideoResult::class.java)

                val listTemp2 = result.results
                for (video in listTemp2) {
                    if (video.site == "YouTube") {
                        listV.add(video)
                    }
                }

                activity?.runOnUiThread {
                    for (v in listV) {
                        adapterV.add(VideoItem(v))
                    }
                    dm_list_video.adapter = adapterV
                    view.findViewById<ProgressBar>(R.id.dm_loading_9).visibility = View.GONE
                }
            }
        })
    }
}

class VideoItem(val video: Video) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.video_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            viewHolder.itemView.video_name.text = video.name

            Picasso.get()
                .load("https://img.youtube.com/vi/${video.key}/hqdefault.jpg")
                .into(viewHolder.itemView.video_image)
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}