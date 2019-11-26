package com.thien.movieplus

import android.content.Intent
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
import kotlinx.android.synthetic.main.fragment_dc_movie.*
import kotlinx.android.synthetic.main.product_of_cast_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDCMovie : Fragment() {

    private var listProductOfCast = ArrayList<ProductOfCast>()
    private val adapterProductOfCast = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dc_movie, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val castId = arguments?.getInt("c_id", -1)
        if (castId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(castId.toString(),view)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.dc_list_movie).layoutManager = layoutManager

        adapterProductOfCast.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailMovieActivity::class.java)
            val myItem = item as ProductOfCastItem
            intent.putExtra("MOVIE_ID", myItem.productOfCast.id)
            intent.putExtra("MOVIE_TITLE", myItem.productOfCast.title)
            intent.putExtra("MOVIE_POSTER", myItem.productOfCast.poster_path)
            intent.putExtra("MOVIE_DATE", myItem.productOfCast.release_date)
            intent.putExtra("MOVIE_VOTE", myItem.productOfCast.vote_average)
            intent.putExtra("MOVIE_BACKDROP", myItem.productOfCast.backdrop_path)
            startActivity(intent)
        }
    }

    private fun fetch(castId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dc_loading_2).visibility = View.VISIBLE
        val url =
            "https://api.themoviedb.org/3/person/$castId/combined_credits?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultProductOfCast::class.java)

                listProductOfCast.clear()
                listProductOfCast = result.cast

                activity?.runOnUiThread {
                    adapterProductOfCast.clear()
                    try {
                        for (m in listProductOfCast) {
                            if (m.media_type == "movie") adapterProductOfCast.add(
                                ProductOfCastItem(
                                    m
                                )
                            )
                        }
                        dc_list_movie.adapter = adapterProductOfCast
                        view.findViewById<ProgressBar>(R.id.dc_loading_2).visibility = View.GONE
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }
                }
            }
        })
    }
}

class ResultProductOfCast(
    val cast: ArrayList<ProductOfCast>
)

class ProductOfCast(
    val id: Int,
    val media_type: String,
    val title: String?,
    val name: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val character: String?
)

class ProductOfCastItem(val productOfCast: ProductOfCast) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.product_of_cast_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        when (productOfCast.media_type) {
            "movie" -> {
                viewHolder.itemView.poc_title.text = productOfCast.title
            }
            "tv" -> {
                viewHolder.itemView.poc_title.text = productOfCast.name
            }
        }

        if (productOfCast.character != null) {
            viewHolder.itemView.poc_char.text = productOfCast.character
        } else {
            viewHolder.itemView.poc_char.text = ""
        }

        try {
            if (productOfCast.poster_path == null) {
                viewHolder.itemView.poc_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + productOfCast.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.poc_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}
