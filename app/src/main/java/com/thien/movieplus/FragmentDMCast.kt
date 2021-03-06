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
import kotlinx.android.synthetic.main.cast_item.view.*
import kotlinx.android.synthetic.main.fragment_dm_cast.*
import kotlinx.android.synthetic.main.people_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDMCast : Fragment() {

    private var listCast = ArrayList<Cast>()
    private val adapterCast = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_cast, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val pref = context!!.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)

        val movieId = arguments?.getInt("m_id", -1)
        if (movieId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetchCast(movieId.toString(), view, english)
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.dm_list_cast).layoutManager = layoutManager

        adapterCast.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailCastActivity::class.java)
            val myItem = item as CastItem
            intent.putExtra("CAST_ID", myItem.cast.id)
            intent.putExtra("CAST_NAME", myItem.cast.name)
            intent.putExtra("CAST_POSTER", myItem.cast.profile_path)
            startActivity(intent)
        }
    }

    private fun fetchCast(movieId: String, view: View, english: Boolean) {
        view.findViewById<ProgressBar>(R.id.dm_loading_2).visibility = View.VISIBLE

        var u = ""
        u = if (english) {
            "https://api.themoviedb.org/3/movie/$movieId/credits?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/movie/$movieId/credits?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request = Request.Builder().url(u).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
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
                    dm_list_cast.adapter = adapterCast
                    view.findViewById<ProgressBar>(R.id.dm_loading_2).visibility = View.GONE
                }
            }
        })
    }
}

class Cast(
    val name: String,
    val profile_path: String?,
    val id: Int,
    val character: String
)

class People(
    val name: String,
    val profile_path: String?,
    val id: Int,
    val known_for_department: String?
)

class CastItem(val cast: Cast) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.cast_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.c_name.text = cast.name
        viewHolder.itemView.c_char.text = cast.character

        try {
            if (cast.profile_path == null) {
                viewHolder.itemView.c_poster.setImageResource(R.drawable.logo_accent)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + cast.profile_path)
                    .noFade()
                    .into(viewHolder.itemView.c_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class PeopleItem(val people: People) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.people_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.p_name.text = people.name

        try {
            if (people.profile_path == null) {
                viewHolder.itemView.p_poster.setImageResource(R.drawable.logo_accent)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + people.profile_path)
                    .noFade()
                    .into(viewHolder.itemView.p_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ResultCast(val cast: List<Cast>)
