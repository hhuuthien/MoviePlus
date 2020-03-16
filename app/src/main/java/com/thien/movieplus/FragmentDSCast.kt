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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_ds_cast.*
import okhttp3.*
import java.io.IOException

class FragmentDSCast : Fragment() {

    private var listCast = ArrayList<Cast>()
    private val adapterCast = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ds_cast, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetchCast(showId.toString(), view)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.ds_list_cast).layoutManager = layoutManager

        adapterCast.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailCastActivity::class.java)
            val myItem = item as CastItem
            intent.putExtra("CAST_ID", myItem.cast.id)
            intent.putExtra("CAST_NAME", myItem.cast.name)
            intent.putExtra("CAST_POSTER", myItem.cast.profile_path)
            startActivity(intent)
        }
    }

    private fun fetchCast(showId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_6).visibility = View.VISIBLE
        val url =
            "https://api.themoviedb.org/3/tv/$showId/credits?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultCast::class.java)

                listCast.clear()
                listCast = result.cast as ArrayList<Cast>

                activity?.runOnUiThread {
                    adapterCast.clear()
                    for (m in listCast) {
                        adapterCast.add(CastItem(m))
                    }
                    ds_list_cast.adapter = adapterCast
                    view.findViewById<ProgressBar>(R.id.dm_loading_6).visibility = View.GONE
                }
            }
        })
    }
}
