package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dm_related.*
import okhttp3.*
import java.io.IOException

class FragmentDMRelated : Fragment() {

    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_related, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val movieId = arguments?.getInt("m_id", -1)
        if (movieId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(movieId.toString())
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.dm_list_related).layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItemRow
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
    }

    private fun fetch(movieId: String) {
        val url =
            "https://api.themoviedb.org/3/movie/$movieId/similar?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                list.clear()
                list = result.results

                activity?.runOnUiThread {
                    adapter.clear()
                    for (m in list) {
                        adapter.add(MovieItemRow(m))
                    }
                    dm_list_related.adapter = adapter
                }
            }
        })
    }
}
