package com.thien.movieplus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_start.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StartActivity : AppCompatActivity() {
    private var sharedPref: SharedPreferences? = null

    private var listMovieNowShowing = ArrayList<Movie>()
    private var listMovieUpComing = ArrayList<Movie>()
    private var listShowAiring = ArrayList<Show>()
    private var listShowNowShowing = ArrayList<Show>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        start_bar.visibility = View.INVISIBLE
        start_text.visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        sharedPref = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val firstRun = sharedPref!!.getBoolean("firstRun", true)
        if (firstRun) {
            sharedPref!!.edit().putBoolean("firstRun", false).apply()
            sharedPref!!.edit().putInt("date", getCurrentDate()).apply()
            fetch1()
        } else {
            val currentDate = getCurrentDate()
            val dateStoring = sharedPref!!.getInt("date", 0)
            if (currentDate > dateStoring) {
                sharedPref!!.edit().putInt("date", currentDate).apply()
                fetch1()
            } else {
                getNotFetch()
            }
        }
    }

    private fun getNotFetch() {
        val json1 = sharedPref!!.getString("json1", null)
        val json2 = sharedPref!!.getString("json2", null)
        val json3 = sharedPref!!.getString("json3", null)
        val json4 = sharedPref!!.getString("json4", null)

        if (json1 != null && json2 != null && json3 != null && json4 != null) {
            val type12 = object : TypeToken<ArrayList<Movie>>() {
            }.type
            listMovieNowShowing = Gson().fromJson(json1, type12)
            listMovieUpComing = Gson().fromJson(json2, type12)

            val type34 = object : TypeToken<ArrayList<Show>>() {
            }.type
            listShowAiring = Gson().fromJson(json3, type34)
            listShowNowShowing = Gson().fromJson(json4, type34)

            val intent = Intent(this@StartActivity, MainActivity::class.java)
            intent.putExtra("listMovieNowShowing", listMovieNowShowing)
            intent.putExtra("listMovieUpComing", listMovieUpComing)
            intent.putExtra("listShowAiring", listShowAiring)
            intent.putExtra("listShowNowShowing", listShowNowShowing)
            startActivity(intent)

            finish()
        } else {
            fetch1()
        }
    }

    private fun fetch1() {
        start_bar.visibility = View.VISIBLE
        start_text.visibility = View.VISIBLE

        val requestNowShowing = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/now_playing?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(requestNowShowing).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar
                    .make(start_layout, "Không có kết nối", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Thử lại") {
                        fetch1()
                    }
                    .setActionTextColor(Color.WHITE)
                    .show()
                start_bar.visibility = View.INVISIBLE
                start_text.visibility = View.INVISIBLE
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMovieNowShowing = result.results

                val json1 = Gson().toJson(listMovieNowShowing)
                sharedPref!!.edit().putString("json1", json1).apply()

                fetch2()
            }
        })
    }

    private fun fetch2() {
        val requestUpComing = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/upcoming?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(requestUpComing).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMovieUpComing = result.results

                val json2 = Gson().toJson(listMovieUpComing)
                sharedPref!!.edit().putString("json2", json2).apply()

                fetch3()
            }
        })
    }

    private fun fetch3() {
        val request1 = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/airing_today?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultShow::class.java)

                listShowAiring = result.results

                val json3 = Gson().toJson(listShowAiring)
                sharedPref!!.edit().putString("json3", json3).apply()

                fetch4()
            }
        })
    }

    private fun fetch4() {
        val request2 = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/on_the_air?api_key=d4a7514dbdd976453d2679e036009283&language=en-US&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultShow::class.java)

                listShowNowShowing = result.results

                val json4 = Gson().toJson(listShowNowShowing)
                sharedPref!!.edit().putString("json4", json4).apply()

                val intent = Intent(this@StartActivity, MainActivity::class.java)
                intent.putExtra("listMovieNowShowing", listMovieNowShowing)
                intent.putExtra("listMovieUpComing", listMovieUpComing)
                intent.putExtra("listShowAiring", listShowAiring)
                intent.putExtra("listShowNowShowing", listShowNowShowing)
                startActivity(intent)

                start_bar.visibility = View.INVISIBLE
                start_text.visibility = View.INVISIBLE
                finish()
            }
        })
    }

    private fun getCurrentDate(): Int {
        val formatter = SimpleDateFormat("yyyyMMdd")
        val date = Date()
        return formatter.format(date).toInt()
    }
}