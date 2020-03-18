package com.thien.movieplus

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.crew_list_layout.view.*
import kotlinx.android.synthetic.main.fragment_ds_info.*
import kotlinx.android.synthetic.main.season_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDSInfo : Fragment() {

    companion object {
        var overview: String = ""
        var website: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ds_info, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val pref = context!!.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(showId.toString(), view, goodquality)
            fetchExID(showId.toString())
        }

        view.findViewById<TextView>(R.id.ds_web).setOnClickListener {
            if (website.isEmpty() || website.isBlank() || website == "" || website == "Đang cập nhật") {
                return@setOnClickListener
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Không thể truy cập địa chỉ này", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        view.findViewById<TextView>(R.id.ds_crew_text).setOnClickListener {
            fetchCrew(showId.toString())
        }
    }

    private fun fetchCrew(showID: String) {
        val myLayout = layoutInflater.inflate(R.layout.crew_list_layout, null)
        myLayout.dm_crew.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val url =
            "https://api.themoviedb.org/3/tv/$showID/credits?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, CrewList::class.java)

                val listCrew: ArrayList<Crew>
                val adapterCrew = GroupAdapter<ViewHolder>()
                listCrew = result.crew

                activity?.runOnUiThread {
                    adapterCrew.clear()
                    for (m in listCrew) {
                        adapterCrew.add(CrewItem(m))
                    }

                    myLayout.dm_crew.adapter = adapterCrew

                    val dialog = AlertDialog.Builder(context)
                        .setView(myLayout)
                        .create()
                    dialog.show()
                }
            }
        })
    }

    private fun fetchExID(id: String) {
        val url =
            "https://api.themoviedb.org/3/tv/$id/external_ids?api_key=d4a7514dbdd976453d2679e036009283&language=en-US"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ShowID::class.java)

                activity?.runOnUiThread {
                    val imdb = result.imdb_id
                    if (imdb == null || imdb == "") {
                        ds_genre.text = "Đang cập nhật"
                        ds_director.text = "Đang cập nhật"
                    } else {
                        fetchRapidAPI(imdb)
                    }
                }
            }
        })
    }

    private fun fetch(showId: String, view: View, goodquality: Boolean) {
        view.findViewById<ProgressBar>(R.id.ds_loading_5).visibility = VISIBLE
        view.findViewById<RelativeLayout>(R.id.ds_info_layout_child).visibility = GONE

        val url =
            "https://api.themoviedb.org/3/tv/$showId?api_key=d4a7514dbdd976453d2679e036009283"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailShow = gSon.fromJson(body, DeShow::class.java)

                overview = if (detailShow.overview == null || detailShow.overview.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailShow.overview
                }

                website = if (detailShow.homepage == null || detailShow.homepage.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailShow.homepage
                }

                activity?.runOnUiThread {
                    try {
                        ds_overview.text = overview
                        ds_web.text = website
                        ds_numEp.text = detailShow.number_of_episodes.toString()

                        val day = detailShow.first_air_date.substring(8, 10)
                        val month = detailShow.first_air_date.substring(5, 7)
                        val year = detailShow.first_air_date.substring(0, 4)
                        ds_date.text = "$day-$month-$year"

                        if (detailShow.last_episode_to_air != null) {
                            val dayLast = detailShow.last_episode_to_air.air_date?.substring(8, 10)
                            val monthLast = detailShow.last_episode_to_air.air_date?.substring(5, 7)
                            val yearLast = detailShow.last_episode_to_air.air_date?.substring(0, 4)

                            ds_last.text =
                                "${detailShow.last_episode_to_air.name}\nTập ${detailShow.last_episode_to_air.episode_number} - Mùa ${detailShow.last_episode_to_air.season_number} ($dayLast-$monthLast-$yearLast)"
                        } else {
                            ds_last.text = "Không có thông tin"
                        }

                        if (detailShow.next_episode_to_air != null) {
                            val dayNext = detailShow.next_episode_to_air.air_date?.substring(8, 10)
                            val monthNext = detailShow.next_episode_to_air.air_date?.substring(5, 7)
                            val yearNext = detailShow.next_episode_to_air.air_date?.substring(0, 4)

                            ds_next.text =
                                "${detailShow.next_episode_to_air.name}\nTập ${detailShow.next_episode_to_air.episode_number} - Mùa ${detailShow.next_episode_to_air.season_number} ($dayNext-$monthNext-$yearNext)"
                        } else {
                            ds_next.text = "Không có thông tin"
                        }

                        val company = detailShow.production_companies
                        if (company == null || company.size == 0) {
                            ds_company.text = "Đang cập nhật"
                        } else if (company.size == 1) {
                            ds_company.text = company[0].name
                        } else {
                            val size = company.size
                            for (i in 0 until size - 1) {
                                ds_company.append(company[i].name + "\n")
                            }
                            ds_company.append(company[size - 1].name)
                        }
                    } catch (e: Exception) {
                        Log.d("error_here", e.toString())
                    }

                    view.findViewById<ProgressBar>(R.id.ds_loading_5).visibility = GONE
                    view.findViewById<RelativeLayout>(R.id.ds_info_layout_child).visibility =
                        VISIBLE
                }
            }
        })
    }

    private fun fetchRapidAPI(imdb_id: String) {
        val client = OkHttpClient()

        val request: Request = Request.Builder()
            .url("https://movie-database-imdb-alternative.p.rapidapi.com/?i=$imdb_id&r=json")
            .get()
            .addHeader("x-rapidapi-host", "movie-database-imdb-alternative.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "c9056de874msh171226ab4868aecp195d81jsn711cc91d5756")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, DetailShowRapidAPI::class.java)

                activity?.runOnUiThread {
                    try {
                        val director = result.Director
                        if (director == null || director == "N/A") {
                            ds_director.text = "Đang cập nhật"
                        } else {
                            ds_director.text = director
                        }

                        val genre = result.Genre
                        if (genre == null || genre == "N/A") {
                            ds_genre.text = "Đang cập nhật"
                        } else {
                            val genreVNese = genre.replace("Action", "Hành động")
                                .replace("Adventure", "Phiêu lưu")
                                .replace("Family", "Gia đình")
                                .replace("Sci-Fi", "Khoa học viễn tưởng")
                                .replace("Comedy", "Hài hước")
                                .replace("Crime", "Tội phạm")
                                .replace("Drama", "Chính kịch")
                                .replace("Thriller", "Hồi hộp")
                                .replace("Horror", "Kinh dị")
                                .replace("Animation", "Hoạt hình")
                                .replace("Fantasy", "Viễn tưởng")
                                .replace("Mystery", "Bí ẩn")
                                .replace("Musical", "Âm nhạc")
                                .replace("Musical", "Âm nhạc")
                                .replace("Romance", "Lãng mạn")
                            ds_genre.text = genreVNese
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.d("error", ex.toString())
                    }
                }
            }
        })
    }
}

class SeasonItem(val season: Season, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.season_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (season.poster_path == null) {
                viewHolder.itemView.ss_poster.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + season.poster_path)
                        .fit()
                        .into(viewHolder.itemView.ss_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + season.poster_path)
                        .fit()
                        .into(viewHolder.itemView.ss_poster)
                }
            }

            viewHolder.itemView.ss_title.text = season.name
            viewHolder.itemView.ss_num_ep.text = "${season.episode_count} tập"

            val day = season.air_date?.substring(8, 10)
            val month = season.air_date?.substring(5, 7)
            val year = season.air_date?.substring(0, 4)
            viewHolder.itemView.ss_date.text = "$day-$month-$year"
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}