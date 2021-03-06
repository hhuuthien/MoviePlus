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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.crew_item.view.*
import kotlinx.android.synthetic.main.crew_list_layout.view.*
import kotlinx.android.synthetic.main.fragment_dm_info.*
import okhttp3.*
import org.jsoup.Jsoup
import java.io.IOException

class FragmentDMInfo : Fragment() {

    companion object {
        var overview: String = ""
        var website: String = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_info, container, false)
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
            fetch(movieId.toString(), view, english)
        }

        view.findViewById<TextView>(R.id.dm_website).setOnClickListener {
            if (website.isEmpty() || website.isBlank() || website == "" || website == "Đang cập nhật" || website == "Updating") {
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

        view.findViewById<TextView>(R.id.dm_crew_text).setOnClickListener {
            fetchCrew(movieId.toString())
        }
    }

    private fun fetch(movieId: String, view: View, english: Boolean) {
        view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = VISIBLE
        view.findViewById<RelativeLayout>(R.id.dm_info_layout_child).visibility = GONE

        var url = ""
        url = if (english) {
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283"
        } else {
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        }

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovie::class.java)

                val imdbId = detailMovie.imdb_id
                if (imdbId != null) {
                    fetchRapidAPI(imdbId, english)
                } else {
                    dm_director.text = "Đang cập nhật"
                    dm_genre.text = "Đang cập nhật"
                    dm_revenue.text = "Không có số liệu"
                }

                overview = if (detailMovie.overview == null || detailMovie.overview.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailMovie.overview
                }

                website = if (detailMovie.homepage == null || detailMovie.homepage.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailMovie.homepage
                }

                activity?.runOnUiThread {
                    dm_overview.text = overview
                    dm_website.text = website

                    val company = detailMovie.production_companies
                    if (company == null || company.size == 0) {
                        dm_company.text = "Đang cập nhật"
                    } else if (company.size == 1) {
                        dm_company.text = company[0].name
                    } else {
                        val size = company.size
                        for (i in 0 until size - 1) {
                            dm_company.append(company[i].name + "\n")
                        }
                        dm_company.append(company[size - 1].name)
                    }

                    if (detailMovie.belongs_to_collection == null) {
                        dm_collection.visibility = GONE
                    } else {
                        //collection
                        dm_collection.visibility = VISIBLE
                        val colText =
                            "Xem series " + detailMovie.belongs_to_collection.name.replace(
                                "Collection",
                                ""
                            )
                        dm_collection.text = colText
                        dm_collection.setOnClickListener {
                            val intent = Intent(context, CollectionActivity::class.java)
                            intent.putExtra("COLLECTION_ID", detailMovie.belongs_to_collection.id)
                            intent.putExtra(
                                "COLLECTION_NAME",
                                detailMovie.belongs_to_collection.name
                            )
                            intent.putExtra(
                                "COLLECTION_BACKDROP",
                                detailMovie.belongs_to_collection.backdrop_path
                            )
                            startActivity(intent)
                        }
                    }

                    view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = GONE
                    view.findViewById<RelativeLayout>(R.id.dm_info_layout_child).visibility =
                        VISIBLE
                }
            }
        })
    }

    private fun fetchCrew(movieId: String) {
        val myLayout = layoutInflater.inflate(R.layout.crew_list_layout, null)
        myLayout.dm_crew.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val url =
            "https://api.themoviedb.org/3/movie/$movieId/credits?api_key=d4a7514dbdd976453d2679e036009283"
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

    private fun fetchRapidAPI(imdb_id: String, english: Boolean) {
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
                val detailMovie = gSon.fromJson(body, DetailMovieRapidAPI::class.java)

                activity?.runOnUiThread {
                    try {
                        val director = detailMovie.Director
                        if (director == null || director == "N/A") {
                            dm_director.text = "Đang cập nhật"
                        } else {
                            dm_director.text = director
                        }

                        val genre = detailMovie.Genre
                        if (genre == null || genre == "N/A") {
                            dm_genre.text = "Đang cập nhật"
                        } else {
                            if (english) {
                                dm_genre.text = genre
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
                                dm_genre.text = genreVNese
                            }
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.d("error", ex.toString())
                    }
                }
            }
        })

        //doanh thu
        val queue = Volley.newRequestQueue(context)
        val url = "https://www.boxofficemojo.com/title/$imdb_id"
        val stringRequest = StringRequest(
            com.android.volley.Request.Method.GET, url,
            com.android.volley.Response.Listener<String> { response ->
                try {
                    val document = Jsoup.parse(response)
                    val elements =
                        document.select("div .a-section .a-spacing-none .mojo-performance-summary .money")
                    val doanhthuDomestic =
                        elements[0].toString().substringAfter(">").substringBefore("<")
                    val doanhthuInternational =
                        elements[1].toString().substringAfter(">").substringBefore("<")
                    val doanhthuWorldwide =
                        elements[2].toString().substringAfter(">").substringBefore("<")

                    activity?.runOnUiThread {
                        dm_revenue.text =
                            "$doanhthuDomestic (Domestic)\n$doanhthuInternational (International)\n$doanhthuWorldwide (Worldwide)"
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        dm_revenue.text = "Không có số liệu"
                    }
                }
            },
            com.android.volley.Response.ErrorListener {
                activity?.runOnUiThread {
                    dm_revenue.text = "Không có số liệu"
                }
            })
        queue.add(stringRequest)
    }
}

class Crew(
    val id: Int,
    val name: String,
    val job: String,
    val profile_path: String?
)

class CrewList(
    val crew: ArrayList<Crew>
)

class CrewItem(private val crew: Crew) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.crew_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (crew.profile_path == null) {
                viewHolder.itemView.cr_poster.setImageResource(R.drawable.logo_accent)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + crew.profile_path)
                    .noFade()
                    .into(viewHolder.itemView.cr_poster)
            }

            viewHolder.itemView.cr_name.text = crew.name
            viewHolder.itemView.cr_job.text = crew.job
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class DetailMovie(
    val backdrop_path: String?,
    val poster_path: String?,
    val title: String,
    val overview: String?,
    val runtime: Int?,
    val homepage: String?,
    val production_companies: ArrayList<Company>?,
    val imdb_id: String?,
    val original_language: String?,
    val belongs_to_collection: CollectionTMDB?
)

class CollectionTMDB(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?
)

class DetailMovieRapidAPI(
    val Director: String?,
    val Runtime: String?,
    val Metascore: String?,
    val imdbRating: String?,
    val Genre: String?
)

class Genre(
    val id: Int,
    val name: String?
)

class Company(
    val id: Int,
    val name: String?
)
