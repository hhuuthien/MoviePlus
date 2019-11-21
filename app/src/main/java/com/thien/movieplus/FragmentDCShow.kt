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
import kotlinx.android.synthetic.main.fragment_dc_show.*
import okhttp3.*
import java.io.IOException

class FragmentDCShow : Fragment() {

    private var listProductOfCast = ArrayList<ProductOfCast>()
    private val adapterProductOfCast = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dc_show, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val castId = arguments?.getInt("c_id", -1)
        if (castId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(castId.toString())
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.dc_list_show).layoutManager = layoutManager

        adapterProductOfCast.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val myItem = item as ProductOfCastItem
            intent.putExtra("SHOW_ID", myItem.productOfCast.id)
            intent.putExtra("SHOW_TITLE", myItem.productOfCast.name)
            intent.putExtra("SHOW_POSTER", myItem.productOfCast.poster_path)
            intent.putExtra("SHOW_BACKDROP", myItem.productOfCast.backdrop_path)
            intent.putExtra("SHOW_VOTE", myItem.productOfCast.vote_average)
            startActivity(intent)
        }
    }

    private fun fetch(castId: String) {
        val url =
            "https://api.themoviedb.org/3/person/$castId/combined_credits?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
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
                            if (m.media_type == "tv") adapterProductOfCast.add(ProductOfCastItem(m))
                        }
                        dc_list_show.adapter = adapterProductOfCast
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }
                }
            }
        })
    }
}
