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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import ir.apend.slider.model.Slide
import ir.apend.slider.ui.Slider
import kotlinx.android.synthetic.main.fragment_show.*
import kotlinx.android.synthetic.main.movie_item.view.*
import okhttp3.*
import java.io.IOException
import java.io.Serializable

class ShowFragment : Fragment() {

    private var listShowAiring = ArrayList<Show>()
    private val adapterShowAiring = GroupAdapter<ViewHolder>()
    private var listShowNowShowing = ArrayList<Show>()
    private val adapterShowNowShowing = GroupAdapter<ViewHolder>()
    private var listShowPopular = ArrayList<Show>()
    private val adapterShowPopular = GroupAdapter<ViewHolder>()
    private var listShowTopRated = ArrayList<Show>()
    private val adapterShowTopRated = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_airing).layoutManager = layoutManager

        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_nowshowing).layoutManager = layoutManager2

        val layoutManager3 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_popular).layoutManager = layoutManager3

        val layoutManager4 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_toprated).layoutManager = layoutManager4

        adapterShowAiring.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterShowNowShowing.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterShowPopular.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterShowTopRated.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        listShowAiring = activity?.intent?.getSerializableExtra("listShowAiring") as ArrayList<Show>
        listShowNowShowing = activity?.intent?.getSerializableExtra("listShowNowShowing") as ArrayList<Show>

        val listBackDrop = ArrayList<String>()
        for (m in listShowAiring) {
            listBackDrop.add("https://image.tmdb.org/t/p/w500/${m.backdrop_path.toString()}")
        }

        val slider = view.findViewById<Slider>(R.id.show_image_slider)
        val slideList = ArrayList<Slide>()
        for (i in 0 until listBackDrop.size) {
            slideList.add(Slide(i,listBackDrop[i],0))
        }
        slider.addSlides(slideList)

        slider.setItemClickListener { _, _, i, _ ->
            val intent = Intent(context, PictureActivity::class.java)
            val path = slideList[i].imageUrl.substringAfter("w500/")
            intent.putExtra("imageString", path)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        adapterShowAiring.clear()
        for (m in listShowAiring) {
            if (m.poster_path != null) adapterShowAiring.add(ShowItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fs_list_airing).adapter = adapterShowAiring

        adapterShowNowShowing.clear()
        for (m in listShowNowShowing) {
            if (m.poster_path != null) adapterShowNowShowing.add(ShowItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fs_list_nowshowing).adapter = adapterShowNowShowing

        fetchPopular(view)
        fetchTopRated(view)
    }

    private fun fetchPopular(view: View) {
        view.findViewById<ProgressBar>(R.id.fs_loading_popular).visibility = VISIBLE
        val request1 = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/popular?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fs_loading_popular).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultShow::class.java)

                listShowPopular.clear()
                listShowPopular = result.results

                activity?.runOnUiThread {
                    adapterShowPopular.clear()
                    for (s in listShowPopular) {
                        adapterShowPopular.add(ShowItem(s))
                    }
                    fs_list_popular.adapter = adapterShowPopular
                    view.findViewById<ProgressBar>(R.id.fs_loading_popular).visibility = GONE
                }
            }
        })
    }
    private fun fetchTopRated(view: View) {
        view.findViewById<ProgressBar>(R.id.fs_loading_toprated).visibility = VISIBLE
        val request1 = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/top_rated?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fs_loading_toprated).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultShow::class.java)

                listShowTopRated.clear()
                listShowTopRated = result.results

                activity?.runOnUiThread {
                    adapterShowTopRated.clear()
                    for (s in listShowTopRated) {
                        adapterShowTopRated.add(ShowItem(s))
                    }
                    fs_list_toprated.adapter = adapterShowTopRated
                    view.findViewById<ProgressBar>(R.id.fs_loading_toprated).visibility = GONE
                }
            }
        })
    }
}

class Show(
    val poster_path: String?,
    val id: Int,
    val name: String,
    val vote_average: Double?,
    val backdrop_path:String?,
    val overview: String?
) : Serializable

class ResultShow(
    val results: ArrayList<Show>
)

class ShowItem(val show: Show) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + show.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.m_poster)
            }

            viewHolder.itemView.m_title.text = show.name
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
