package com.thien.movieplus

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import ir.apend.slider.model.Slide
import ir.apend.slider.ui.Slider
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.genre_item.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.load_more.view.*
import kotlinx.android.synthetic.main.movie_item.view.*
import kotlinx.android.synthetic.main.movie_item_row.view.*
import kotlinx.android.synthetic.main.movie_item_small.view.*
import okhttp3.*
import java.io.IOException
import java.io.Serializable

class MainFragment : Fragment() {

    private var listMovieNowShowing = ArrayList<Movie>()
    private var listMovieUpComing = ArrayList<Movie>()
    private var listMoviePopular = ArrayList<Movie>()
    private var listMovieList = ArrayList<ListMain>()
    private var listMovieList2 = ArrayList<ListMain>()
    private var listShowAiring = ArrayList<Show>()
    private var listShowNowShowing = ArrayList<Show>()
    private var listShowPopular = ArrayList<Show>()
    private var listNetflix = ArrayList<Show>()
    private var listNetflixM = ArrayList<Movie>()

    private val adapterNowShowing = GroupAdapter<ViewHolder>()
    private val adapterUpComing = GroupAdapter<ViewHolder>()
    private val adapterPopular = GroupAdapter<ViewHolder>()
    private val adapterList = GroupAdapter<ViewHolder>()
    private val adapterList2 = GroupAdapter<ViewHolder>()
    private val adapterShowAiring = GroupAdapter<ViewHolder>()
    private val adapterShowNowShowing = GroupAdapter<ViewHolder>()
    private val adapterShowPopular = GroupAdapter<ViewHolder>()
    private val adapterPeople = GroupAdapter<ViewHolder>()
    private val adapterNetflix = GroupAdapter<ViewHolder>()
    private val adapterNetflixM = GroupAdapter<ViewHolder>()

    private var stringNetflixID = ""
    private var stringNetflixMovieID = ""
    private val slideList = ArrayList<Slide>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val pref = context!!.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_popular).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_list).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_list2).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_category).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_airing).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_nowshowing).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_popular).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fp_list_popular).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fs_list_netflix).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_netflix).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        if (english) {
            listMovieList.add(ListMain(1, "Marvel Cinematic Universe", R.drawable.marvel))
            listMovieList.add(ListMain(2, "Popular Vietnamese movies", R.drawable.vietnam))
            listMovieList.add(ListMain(3, "Pixar animated movies", R.drawable.pixar))
            listMovieList.add(ListMain(4, "Oscars for Best Animated Movies", R.drawable.oscar))
            listMovieList.add(ListMain(5, "Oscars for Best Picture", R.drawable.oscar2))
            listMovieList.shuffle()
        } else {
            listMovieList.add(ListMain(1, "Vũ trụ điện ảnh Marvel", R.drawable.marvel))
            listMovieList.add(ListMain(2, "Phim Việt Nam nổi bật", R.drawable.vietnam))
            listMovieList.add(ListMain(3, "Phim hoạt hình Pixar", R.drawable.pixar))
            listMovieList.add(ListMain(4, "Giải Oscars phim hoạt hình hay nhất", R.drawable.oscar))
            listMovieList.add(ListMain(5, "Giải Oscars phim hay nhất", R.drawable.oscar2))
            listMovieList.shuffle()
        }

        for (m in listMovieList) {
            adapterList.add(ListItemMain(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_list).adapter = adapterList

        adapterList.setOnItemClickListener { item, _ ->
            val myItem = item as ListItemMain
            when (myItem.list.id) {
                1 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 1)//marvel
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 2)//vietnam
                    startActivity(intent)
                }
                3 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 3)//pixar
                    startActivity(intent)
                }
                4 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 4)//oscar
                    startActivity(intent)
                }
                5 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 5)//oscar2
                    startActivity(intent)
                }
            }
        }

        if (english) {
            listMovieList2.add(ListMain(6, "Top 20 highest-grossing movies", R.drawable.top20))
            listMovieList2.add(
                ListMain(
                    7,
                    "Top 15 IMDb highest-rating movies",
                    R.drawable.imdblist
                )
            )
            listMovieList2.add(
                ListMain(
                    8,
                    "Top 20 highest-grossing animated movies",
                    R.drawable.animation
                )
            )
            listMovieList2.shuffle()
        } else {
            listMovieList2.add(ListMain(6, "Top 20 phim doanh thu cao nhất", R.drawable.top20))
            listMovieList2.add(ListMain(7, "Top 15 phim điểm IMDb cao nhất", R.drawable.imdblist))
            listMovieList2.add(
                ListMain(
                    8,
                    "Top 20 phim hoạt hình doanh thu cao nhất",
                    R.drawable.animation
                )
            )
            listMovieList2.shuffle()
        }


        for (m in listMovieList2) {
            adapterList2.add(ListItemMain(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_list2).adapter = adapterList2

        adapterList2.setOnItemClickListener { item, _ ->
            val myItem = item as ListItemMain
            when (myItem.list.id) {
                6 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 6)//top20
                    startActivity(intent)
                }
                7 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 7)//imdb
                    startActivity(intent)
                }
                8 -> {
                    val intent = Intent(context, ListActivity::class.java)
                    intent.putExtra("type", 8)//animation
                    startActivity(intent)
                }
            }
        }

        val listGenre = ArrayList<Genre>()
        if (!english) {
            listGenre.add(Genre(28, "Hành động"))
            listGenre.add(Genre(53, "Hồi hộp"))
            listGenre.add(Genre(9648, "Bí ẩn"))
            listGenre.add(Genre(14, "Giả tưởng"))
            listGenre.add(Genre(36, "Lịch sử"))
            listGenre.add(Genre(12, "Phiêu lưu"))
            listGenre.add(Genre(80, "Tội phạm"))
            listGenre.add(Genre(16, "Hoạt hình"))
            listGenre.add(Genre(878, "Khoa học viễn tưởng"))
            listGenre.add(Genre(35, "Hài kịch"))
            listGenre.add(Genre(10749, "Lãng mạn"))
            listGenre.add(Genre(27, "Kinh dị"))
            listGenre.add(Genre(18, "Chính kịch"))
            listGenre.add(Genre(10751, "Gia đình"))
            listGenre.add(Genre(10402, "Âm nhạc"))
            listGenre.add(Genre(10752, "Chiến tranh"))
        } else {
            listGenre.add(Genre(28, "Action"))
            listGenre.add(Genre(53, "Thriller"))
            listGenre.add(Genre(9648, "Mystery"))
            listGenre.add(Genre(14, "Fantasy"))
            listGenre.add(Genre(36, "History"))
            listGenre.add(Genre(12, "Advanture"))
            listGenre.add(Genre(80, "Crime"))
            listGenre.add(Genre(16, "Animation"))
            listGenre.add(Genre(878, "Sci-Fi"))
            listGenre.add(Genre(35, "Comedy"))
            listGenre.add(Genre(10749, "Romance"))
            listGenre.add(Genre(27, "Horror"))
            listGenre.add(Genre(18, "Drama"))
            listGenre.add(Genre(10751, "Family"))
            listGenre.add(Genre(10402, "Musical"))
            listGenre.add(Genre(10752, "War"))
        }

        listGenre.shuffle()

        val adapterGenre = GroupAdapter<ViewHolder>()
        for (m in listGenre) {
            adapterGenre.add(GenreItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_category).adapter = adapterGenre

        adapterGenre.setOnItemClickListener { item, _ ->
            val myItem = item as GenreItem
            val intent = Intent(context, DiscoverMovieActivity::class.java)
            intent.putExtra("genre_id", myItem.genre.id)
            startActivity(intent)
        }

        adapterNowShowing.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            val intent = Intent(context, DetailMovieActivity::class.java)
                .putExtra("MOVIE_ID", myItem.movie.id)
                .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                .putExtra("MOVIE_DATE", myItem.movie.release_date)
                .putExtra("MOVIE_TITLE", myItem.movie.title)
            startActivity(intent)
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

        adapterShowAiring.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterShowNowShowing.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterShowPopular.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        adapterNetflix.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            intent.putExtra("NETFLIX_ID", stringNetflixID)
            startActivity(intent)
        }

        adapterNetflixM.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
                    .putExtra("NETFLIX_ID", stringNetflixMovieID)
            )
        }

        adapterPeople.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailCastActivity::class.java)
            val myItem = item as PeopleItem
            intent.putExtra("CAST_ID", myItem.people.id)
            intent.putExtra("CAST_NAME", myItem.people.name)
            intent.putExtra("CAST_POSTER", myItem.people.profile_path)
            startActivity(intent)
        }

        view.findViewById<Slider>(R.id.image_slider).setItemClickListener { _, _, i, _ ->
            try {
                var movie = Movie("", "", 0, "", "", 0.0, "")
                val mID = slideList[i].id
                for (m in listMovieNowShowing) {
                    if (m.id == mID) {
                        movie = m
                        break
                    }
                }
                if (movie.title != "") {
                    startActivity(
                        Intent(context, DetailMovieActivity::class.java)
                            .putExtra("MOVIE_ID", movie.id)
                            .putExtra("MOVIE_POSTER", movie.poster_path)
                            .putExtra("MOVIE_BACKDROP", movie.backdrop_path)
                            .putExtra("MOVIE_TITLE", movie.title)
                            .putExtra("MOVIE_VOTE", movie.vote_average)
                            .putExtra("MOVIE_DATE", movie.release_date)
                    )
                }
            } catch (e: Exception) {
                Log.d("error_here", e.toString())
            }
        }

        fetchNowShowing(view, english, goodquality)
        fetchUpComing(view, english, goodquality)
        fetchPopular(view, english, goodquality)
        fetchShowAiring(view, english, goodquality)
        fetchShowNowShowing(view, english, goodquality)
        fetchShowPopular(view, english, goodquality)
        fetchCast(view)
        fetchNetflixShow(view, english, goodquality)
        fetchNetflixMovie(view, english, goodquality)
    }

    private fun fetchNowShowing(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_NowShowing_Movie", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            val url: String = if (english) {
                "https://api.themoviedb.org/3/movie/now_playing?api_key=d4a7514dbdd976453d2679e036009283&region=VN"
            } else {
                "https://api.themoviedb.org/3/movie/now_playing?api_key=d4a7514dbdd976453d2679e036009283&region=VN&language=vi"
            }
            val requestNowShowing = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(requestNowShowing).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, Result::class.java)

                    listMovieNowShowing.clear()
                    listMovieNowShowing = result.results

                    activity?.runOnUiThread {
                        adapterNowShowing.clear()
                        for (m in listMovieNowShowing) {
                            if (m.poster_path != null) adapterNowShowing.add(
                                MovieItem(
                                    m,
                                    goodquality
                                )
                            )
                        }
                        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).adapter =
                            adapterNowShowing

                        slideList.clear()
                        if (goodquality) {
                            for (m in listMovieNowShowing) {
                                if (m.backdrop_path != null) {
                                    slideList.add(
                                        Slide(
                                            m.id,
                                            "https://image.tmdb.org/t/p/w500/${m.backdrop_path}",
                                            0
                                        )
                                    )
                                }
                            }
                        } else {
                            for (m in listMovieNowShowing) {
                                if (m.backdrop_path != null) {
                                    slideList.add(
                                        Slide(
                                            m.id,
                                            "https://image.tmdb.org/t/p/w300/${m.backdrop_path}",
                                            0
                                        )
                                    )
                                }
                            }
                        }
                        view.findViewById<Slider>(R.id.image_slider).addSlides(slideList)
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, Result::class.java)

            listMovieNowShowing.clear()
            listMovieNowShowing = result.results

            adapterNowShowing.clear()
            for (m in listMovieNowShowing) {
                if (m.poster_path != null) adapterNowShowing.add(MovieItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).adapter =
                adapterNowShowing

            slideList.clear()
            if (goodquality) {
                for (m in listMovieNowShowing) {
                    if (m.backdrop_path != null) {
                        slideList.add(
                            Slide(
                                m.id,
                                "https://image.tmdb.org/t/p/w500/${m.backdrop_path}",
                                0
                            )
                        )
                    }
                }
            } else {
                for (m in listMovieNowShowing) {
                    if (m.backdrop_path != null) {
                        slideList.add(
                            Slide(
                                m.id,
                                "https://image.tmdb.org/t/p/w300/${m.backdrop_path}",
                                0
                            )
                        )
                    }
                }
            }
            view.findViewById<Slider>(R.id.image_slider).addSlides(slideList)
        }
    }

    private fun fetchUpComing(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Upcoming_Movie", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/3/movie/upcoming?api_key=d4a7514dbdd976453d2679e036009283&region=VN"
            } else {
                "https://api.themoviedb.org/3/movie/upcoming?api_key=d4a7514dbdd976453d2679e036009283&region=VN&language=vi"
            }
            val requestUpComing = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(requestUpComing).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, Result::class.java)

                    listMovieUpComing.clear()
                    listMovieUpComing = result.results
                    listMovieUpComing.sortWith(compareBy { it.release_date })

                    activity?.runOnUiThread {
                        adapterUpComing.clear()
                        for (m in listMovieUpComing) {
                            if (m.poster_path != null) adapterUpComing.add(
                                MovieItem(
                                    m,
                                    goodquality
                                )
                            )
                        }
                        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).adapter =
                            adapterUpComing
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, Result::class.java)

            listMovieUpComing.clear()
            listMovieUpComing = result.results
            listMovieUpComing.sortWith(compareBy { it.release_date })

            adapterUpComing.clear()
            for (m in listMovieUpComing) {
                if (m.poster_path != null) adapterUpComing.add(MovieItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fm_list_upcoming).adapter = adapterUpComing
        }
    }

    private fun fetchPopular(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Popular_Movie", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/3/movie/popular?api_key=d4a7514dbdd976453d2679e036009283&region=VN"
            } else {
                "https://api.themoviedb.org/3/movie/popular?api_key=d4a7514dbdd976453d2679e036009283&region=VN&language=vi"
            }
            val request = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, Result::class.java)

                    listMoviePopular.clear()
                    listMoviePopular = result.results

                    activity?.runOnUiThread {
                        adapterPopular.clear()
                        for (m in listMoviePopular) {
                            adapterPopular.add(MovieItem(m, goodquality))
                        }
                        fm_list_popular.adapter = adapterPopular
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, Result::class.java)

            listMoviePopular.clear()
            listMoviePopular = result.results

            adapterPopular.clear()
            for (m in listMoviePopular) {
                adapterPopular.add(MovieItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fm_list_popular).adapter = adapterPopular
        }
    }

    private fun fetchShowAiring(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Airing_Show", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/3/tv/airing_today?api_key=d4a7514dbdd976453d2679e036009283&region=US&timezone=VN"
            } else {
                "https://api.themoviedb.org/3/tv/airing_today?api_key=d4a7514dbdd976453d2679e036009283&language=vi&region=US&timezone=VN"
            }
            val request1 = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(request1).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, ResultShow::class.java)

                    listShowAiring.clear()
                    listShowAiring = result.results

                    activity?.runOnUiThread {
                        adapterShowAiring.clear()
                        for (m in listShowAiring) {
                            if (m.poster_path != null) adapterShowAiring.add(
                                ShowItem(
                                    m,
                                    goodquality
                                )
                            )
                        }
                        view.findViewById<RecyclerView>(R.id.fs_list_airing).adapter =
                            adapterShowAiring
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, ResultShow::class.java)

            listShowAiring.clear()
            listShowAiring = result.results

            adapterShowAiring.clear()
            for (m in listShowAiring) {
                if (m.poster_path != null) adapterShowAiring.add(ShowItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fs_list_airing).adapter = adapterShowAiring
        }
    }

    private fun fetchShowNowShowing(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_NowShowing_Show", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/3/tv/on_the_air?api_key=d4a7514dbdd976453d2679e036009283&region=US"
            } else {
                "https://api.themoviedb.org/3/tv/on_the_air?api_key=d4a7514dbdd976453d2679e036009283&language=vi&region=US"
            }
            val request2 = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(request2).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, ResultShow::class.java)

                    listShowNowShowing.clear()
                    listShowNowShowing = result.results

                    activity?.runOnUiThread {
                        adapterShowNowShowing.clear()
                        for (m in listShowNowShowing) {
                            if (m.poster_path != null) adapterShowNowShowing.add(
                                ShowItem(
                                    m,
                                    goodquality
                                )
                            )
                        }
                        view.findViewById<RecyclerView>(R.id.fs_list_nowshowing).adapter =
                            adapterShowNowShowing
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, ResultShow::class.java)

            listShowNowShowing.clear()
            listShowNowShowing = result.results

            adapterShowNowShowing.clear()
            for (m in listShowNowShowing) {
                if (m.poster_path != null) adapterShowNowShowing.add(
                    ShowItem(
                        m,
                        goodquality
                    )
                )
            }
            view.findViewById<RecyclerView>(R.id.fs_list_nowshowing).adapter = adapterShowNowShowing
        }
    }

    private fun fetchNetflixShow(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Netflix_Show", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/4/list/136507?api_key=d4a7514dbdd976453d2679e036009283"
            } else {
                "https://api.themoviedb.org/4/list/136507?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
            }

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, MyListShowV4::class.java)

                    listNetflix.clear()
                    listNetflix = result.results
                    listNetflix.shuffle()

                    stringNetflixID = body!!.substringAfter("comments").substringBefore("sort_by")

                    activity?.runOnUiThread {
                        adapterNetflix.clear()
                        for (m in listNetflix) {
                            adapterNetflix.add(ShowItem(m, goodquality))
                        }
                        fs_list_netflix.adapter = adapterNetflix
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, MyListShowV4::class.java)

            listNetflix.clear()
            listNetflix = result.results
            listNetflix.shuffle()

            stringNetflixID = data.substringAfter("comments").substringBefore("sort_by")

            adapterNetflix.clear()
            for (m in listNetflix) {
                adapterNetflix.add(ShowItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fs_list_netflix).adapter = adapterNetflix
        }
    }

    private fun fetchNetflixMovie(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Netflix_Movie", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/4/list/136515?api_key=d4a7514dbdd976453d2679e036009283"
            } else {
                "https://api.themoviedb.org/4/list/136515?api_key=d4a7514dbdd976453d2679e036009283&language=vi"
            }

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, MyListV4::class.java)

                    listNetflixM.clear()
                    listNetflixM = result.results
                    listNetflixM.shuffle()

                    stringNetflixMovieID =
                        body!!.substringAfter("comments").substringBefore("sort_by")

                    activity?.runOnUiThread {
                        adapterNetflixM.clear()
                        for (m in listNetflixM) {
                            adapterNetflixM.add(MovieItem(m, goodquality))
                        }
                        fm_list_netflix.adapter = adapterNetflixM
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, MyListV4::class.java)

            listNetflixM.clear()
            listNetflixM = result.results
            listNetflixM.shuffle()

            stringNetflixMovieID = data.substringAfter("comments").substringBefore("sort_by")

            adapterNetflixM.clear()
            for (m in listNetflixM) {
                adapterNetflixM.add(MovieItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fm_list_netflix).adapter = adapterNetflixM
        }
    }

    private fun fetchShowPopular(view: View, english: Boolean, goodquality: Boolean) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Popular_Show", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            var url = ""
            url = if (english) {
                "https://api.themoviedb.org/3/tv/popular?api_key=d4a7514dbdd976453d2679e036009283&region=US"
            } else {
                "https://api.themoviedb.org/3/tv/popular?api_key=d4a7514dbdd976453d2679e036009283&language=vi&region=US"
            }
            val request2 = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(request2).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, ResultShow::class.java)

                    listShowPopular.clear()
                    listShowPopular = result.results

                    activity?.runOnUiThread {
                        adapterShowPopular.clear()
                        for (m in listShowPopular) {
                            if (m.poster_path != null) adapterShowPopular.add(
                                ShowItem(
                                    m,
                                    goodquality
                                )
                            )
                        }
                        view.findViewById<RecyclerView>(R.id.fs_list_popular).adapter =
                            adapterShowPopular
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, ResultShow::class.java)

            listShowPopular.clear()
            listShowPopular = result.results

            adapterShowPopular.clear()
            for (m in listShowPopular) {
                if (m.poster_path != null) adapterShowPopular.add(ShowItem(m, goodquality))
            }
            view.findViewById<RecyclerView>(R.id.fs_list_popular).adapter = adapterShowPopular
        }
    }

    private fun fetchCast(view: View) {
        val dataPref1 = context!!.getSharedPreferences("DataPref_Cast", 0)

        val data = dataPref1.getString("data", "nothing")
        val savedTime = dataPref1.getLong("time", 0)
        val time = System.currentTimeMillis() - savedTime

        if (data == null || data.isBlank() || data.isEmpty() || data == "nothing" || time > 86400000) {
            val url =
                "https://api.themoviedb.org/3/trending/person/day?api_key=d4a7514dbdd976453d2679e036009283"
            val request = Request.Builder()
                .url(url)
                .build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    //put to Share Preferences
                    val editor1: SharedPreferences.Editor = dataPref1.edit()
                    editor1.putString("data", body)
                    editor1.putLong("time", System.currentTimeMillis())
                    editor1.apply()
                    val gSon = GsonBuilder().create()
                    val result = gSon.fromJson(body, TrendingPeopleClass::class.java)
                    val list = result.results

                    activity?.runOnUiThread {
                        adapterPeople.clear()
                        for (m in list) {
                            if (m.known_for_department == "Acting") {
                                adapterPeople.add(PeopleItem(m))
                            }
                        }
                        view.findViewById<RecyclerView>(R.id.fp_list_popular).adapter =
                            adapterPeople
                    }
                }
            })
        } else {
            val gSon = GsonBuilder().create()
            val result = gSon.fromJson(data, TrendingPeopleClass::class.java)
            val list = result.results

            adapterPeople.clear()
            for (m in list) {
                if (m.known_for_department == "Acting") {
                    adapterPeople.add(PeopleItem(m))
                }
            }
            view.findViewById<RecyclerView>(R.id.fp_list_popular).adapter = adapterPeople
        }
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

class Cinema(
    var cumrap: String,
    var tenrap: String,
    var diachi: String,
    var thanhpho: String,
    var quan: String,
    var gioithieu: String,
    var lat: Double?,
    var lng: Double?
) : Serializable

class Result(val results: ArrayList<Movie>)

class MovieItem(val movie: Movie, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster)
                }
            }

//            viewHolder.itemView.m_title.text = movie.title
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class MovieItemSmall(val movie: Movie, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_small
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.m_poster_small.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster_small)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster_small)
                }
            }

//            viewHolder.itemView.m_title_small.text = movie.title
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ListItemMain(val list: ListMain) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.list_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            viewHolder.itemView.list_item_image.setImageResource(list.image)
            viewHolder.itemView.list_item_name.text = list.name
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class MovieItemRow(val movie: Movie, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.mrow_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + movie.poster_path)
                        .fit()
                        .into(viewHolder.itemView.mrow_poster)
                }
            }

            viewHolder.itemView.mrow_title.text = movie.title
            viewHolder.itemView.mrow_description.text = movie.overview
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ShowItemRow(val show: Show, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.mrow_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.mrow_poster)
                }
            }

            viewHolder.itemView.mrow_title.text = show.name
            viewHolder.itemView.mrow_description.text = show.overview
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class GenreItem(val genre: Genre) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.genre_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.genre_name.text = genre.name
    }
}

class ListMain(
    var id: Int,
    var name: String,
    var image: Int
)

class Show(
    val poster_path: String?,
    val id: Int,
    val name: String,
    val vote_average: Double?,
    val backdrop_path: String?,
    val overview: String?
) : Serializable

class Season(
    val air_date: String?,
    val episode_count: Int,
    val id: Int,
    val name: String,
    val poster_path: String?,
    val season_number: Int?
) : Serializable

class ResultShow(
    val results: ArrayList<Show>
)

class TrendingPeopleClass(
    val results: ArrayList<People>
)

class ShowItem(val show: Show, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster)
                }
            }

//            viewHolder.itemView.m_title.text = show.name
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ShowItemSmall(val show: Show, val goodquality: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_small
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.m_poster_small.setImageResource(R.drawable.logo_accent)
            } else {
                if (goodquality) {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w500" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster_small)
                } else {
                    Picasso.get()
                        .load("https://image.tmdb.org/t/p/w200" + show.poster_path)
                        .fit()
                        .into(viewHolder.itemView.m_poster_small)
                }
            }

//            viewHolder.itemView.m_title_small.text = show.name
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ItemLoadMore(var spin: Boolean) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.load_more
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (spin) {
            viewHolder.itemView.loadmore_load.visibility = VISIBLE
            viewHolder.itemView.loadmore_text.visibility = GONE
        } else {
            viewHolder.itemView.loadmore_load.visibility = GONE
            viewHolder.itemView.loadmore_text.visibility = VISIBLE
        }
    }
}