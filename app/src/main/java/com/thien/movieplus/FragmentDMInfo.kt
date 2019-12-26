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
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_dm_info.*
import kotlinx.android.synthetic.main.text_layout.view.*
import okhttp3.*
import java.io.IOException

class FragmentDMInfo : Fragment() {

    companion object {
        var overview: String = ""
        var website: String = ""
        var tagline: String = ""
        var budget: Long = -1
        var revenue: Long = -1
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
        val movieId = arguments?.getInt("m_id", -1)
        if (movieId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(movieId.toString(), view)
        }

        view.findViewById<TextView>(R.id.dm_overview).setOnClickListener {
            val myLayout = layoutInflater.inflate(R.layout.text_layout, null)
            myLayout.textview.text = overview
            val dialog = AlertDialog.Builder(context!!)
                .setView(myLayout)
                .create()
            dialog.show()
        }

        view.findViewById<TextView>(R.id.dm_website).setOnClickListener {
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
    }

    private fun fetch(movieId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = VISIBLE
        view.findViewById<RelativeLayout>(R.id.dm_info_layout_child).visibility = GONE

        val url =
            "https://api.themoviedb.org/3/movie/$movieId?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val detailMovie = gSon.fromJson(body, DetailMovie::class.java)

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

                tagline = if (detailMovie.tagline == null || detailMovie.tagline.isEmpty()) {
                    "Đang cập nhật"
                } else {
                    detailMovie.tagline
                }

                budget = detailMovie.budget ?: -1
                revenue = detailMovie.revenue ?: -1

                activity?.runOnUiThread {
                    dm_overview.text = overview
                    dm_website.text = website
                    dm_tag.text = tagline

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

                    when (budget) {
                        (-1).toLong() -> {
                            dm_budget.text = "Đang cập nhật"
                        }
                        0.toLong() -> {
                            dm_budget.text = "Không có số liệu"
                        }
                        else -> {
                            dm_budget.text = formatBudget(budget) + " (USD)"
                        }
                    }

                    when (revenue) {
                        (-1).toLong() -> {
                            dm_revenue.text = "Đang cập nhật"
                        }
                        0.toLong() -> {
                            dm_revenue.text = "Không có số liệu"
                        }
                        else -> {
                            dm_revenue.text = formatBudget(revenue) + " (USD)"
                        }
                    }

                    view.findViewById<ProgressBar>(R.id.dm_loading_1).visibility = GONE
                    view.findViewById<RelativeLayout>(R.id.dm_info_layout_child).visibility =
                        VISIBLE
                }
            }
        })
    }

    private fun formatBudget(num: Long): String {
        val s = num.toString()
        val n = s.length
        var s1 = ""
        for (i in n - 1 downTo 0) {
            s1 += s[i]
        }
        val nn = s1.length
        if (nn == n) {
            var s2 = ""
            for (i in nn - 1 downTo 0) {
                s2 += s1[i]
                if (i % 3 == 0) {
                    s2 += " "
                }
            }
            return s2
        } else {
            return "NULL"
        }
    }
}

class DetailMovie(
    val backdrop_path: String?,
    val poster_path: String?,
    val title: String,
    val overview: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val release_date: String?,
    val runtime: Int?,
    val genres: ArrayList<Genre>,
    val original_language: String?,
    val tagline: String?,
    val homepage: String?,
    val production_companies: ArrayList<Company>?,
    val budget: Long?,
    val revenue: Long?
)

class Genre(
    val id: Int,
    val name: String?
)

class Company(
    val id: Int,
    val name: String?
)
