package com.thien.movieplus

import android.content.Intent
import android.graphics.Color
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
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.fragment_movie.*
import kotlinx.android.synthetic.main.movie_item.view.*
import kotlinx.android.synthetic.main.movie_item_row.view.*
import okhttp3.*
import java.io.IOException
import java.io.Serializable

class MovieFragment : Fragment() {

    private var listMovieNowShowing = ArrayList<Movie>()
    private var listMovieUpComing = ArrayList<Movie>()
    private var listMoviePopular = ArrayList<Movie>()
    private var listMovieTopRated = ArrayList<Movie>()
    private val adapterNowShowing = GroupAdapter<ViewHolder>()
    private val adapterUpComing = GroupAdapter<ViewHolder>()
    private val adapterPopular = GroupAdapter<ViewHolder>()
    private val adapterTopRated = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val layoutManager1 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).layoutManager = layoutManager1

        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).layoutManager = layoutManager2

        val layoutManager3 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_popular).layoutManager = layoutManager3

        val layoutManager4 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_toprated).layoutManager = layoutManager4

        adapterNowShowing.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
            )
        }

        adapterUpComing.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
            )
        }

        adapterPopular.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
            )
        }

        adapterTopRated.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
            )
        }

        listMovieNowShowing =
            activity?.intent?.getSerializableExtra("listMovieNowShowing") as ArrayList<Movie>
        listMovieUpComing =
            activity?.intent?.getSerializableExtra("listMovieUpComing") as ArrayList<Movie>

        adapterNowShowing.clear()
        for (m in listMovieNowShowing) {
            if (m.poster_path != null) adapterNowShowing.add(MovieItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).adapter = adapterNowShowing

        adapterUpComing.clear()
        for (m in listMovieUpComing) {
            if (m.poster_path != null) adapterUpComing.add(MovieItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).adapter = adapterUpComing

        fetchPopular(view)
        fetchTopRated(view)
    }

    private fun fetchPopular(view: View) {
        view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = VISIBLE
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/popular?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMoviePopular.clear()
                listMoviePopular = result.results

                activity?.runOnUiThread {
                    adapterPopular.clear()
                    for (m in listMoviePopular) {
                        adapterPopular.add(MovieItem(m))
                    }
                    fm_list_popular.adapter = adapterPopular
                    view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = GONE
                }
            }
        })
    }
    private fun fetchTopRated(view: View) {
        view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = VISIBLE
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/top_rated?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMovieTopRated.clear()
                listMovieTopRated = result.results

                activity?.runOnUiThread {
                    adapterTopRated.clear()
                    for (m in listMovieTopRated) {
                        adapterTopRated.add(MovieItem(m))
                    }
                    fm_list_toprated.adapter = adapterTopRated
                    view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = GONE
                }
            }
        })
    }
}

class Movie(
    val poster_path: String?,
    val backdrop_path: String?,
    val id: Int,
    val title: String,
    val release_date: String?,
    val vote_average: Double?,
    val overview: String?
) : Serializable

class MovieLove(
    var id: Int,
    var title: String,
    var poster:String?,
    var backdrop: String?,
    var vote:Double,
    var date:String?
) : Serializable

class Result(val results: ArrayList<Movie>)

class MovieItem(val movie: Movie) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + movie.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.m_poster)
            }

            viewHolder.itemView.m_title.text = movie.title
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class MovieItemRow(val movie: Movie) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w200" + movie.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.mrow_poster)
            }

            viewHolder.itemView.mrow_title.text = movie.title
            viewHolder.itemView.mrow_description.text = movie.overview
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ShowItemRow(val show: Show) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + show.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.mrow_poster)
            }

            viewHolder.itemView.mrow_title.text = show.name
            viewHolder.itemView.mrow_description.text = show.overview
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
