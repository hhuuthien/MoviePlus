package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import ir.apend.slider.model.Slide
import ir.apend.slider.ui.Slider
import kotlinx.android.synthetic.main.fragment_movie.*
import kotlinx.android.synthetic.main.genre_item.view.*
import kotlinx.android.synthetic.main.movie_item.view.*
import kotlinx.android.synthetic.main.movie_item_row.view.*
import okhttp3.*
import java.io.IOException
import java.io.Serializable

class MovieFragment : Fragment() {

    private var listMovieNowShowing = ArrayList<Movie>()
    private var listMovieUpComing = ArrayList<Movie>()
    private var listMoviePopular = ArrayList<Movie>()
    private var listMovieTopRated = ArrayList<Movie>()
    private val adapterNowShowing = GroupAdapter<ViewHolder>()
    private val adapterUpComing = GroupAdapter<ViewHolder>()
    private val adapterPopular = GroupAdapter<ViewHolder>()
    private val adapterTopRated = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val layoutManager1 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).layoutManager = layoutManager1

        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).layoutManager = layoutManager2

        val layoutManager3 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_popular).layoutManager = layoutManager3

        val layoutManager4 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.findViewById<RecyclerView>(R.id.fm_list_toprated).layoutManager = layoutManager4

        val layoutManager5 = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        view.findViewById<RecyclerView>(R.id.fm_list_genre).layoutManager = layoutManager5

        val listGenre = ArrayList<Genre>()
        listGenre.add(Genre(28, "Hành động"))
        listGenre.add(Genre(16, "Hoạt hình"))
        listGenre.add(Genre(878, "Khoa học viễn tưởng"))
        listGenre.add(Genre(35, "Hài kịch"))
        listGenre.add(Genre(10749, "Lãng mạn"))
        listGenre.add(Genre(27, "Kinh dị"))
        listGenre.add(Genre(80, "Tội phạm"))
        listGenre.add(Genre(18, "Chính kịch"))
        listGenre.add(Genre(10751, "Gia đình"))
        listGenre.add(Genre(10402, "Âm nhạc"))
        listGenre.add(Genre(10752, "Chiến tranh"))

        listGenre.shuffle()

        val adapterGenre = GroupAdapter<ViewHolder>()
        for (m in listGenre) {
            adapterGenre.add(GenreItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_genre).adapter = adapterGenre

        adapterGenre.setOnItemClickListener { item, _ ->
            val myItem = item as GenreItem
            val intent = Intent(context, ListByGenreActivity::class.java)
            intent.putExtra("genre_id", myItem.genre.id)
            intent.putExtra("genre_name", myItem.genre.name)
            startActivity(intent)
        }

        view.findViewById<RelativeLayout>(R.id.flm_1).setOnClickListener {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("type", 1)//marvel
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_2).setOnClickListener {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("type", 2)//vietnam
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_3).setOnClickListener {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("type", 3)//pixar
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_4).setOnClickListener {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra("type", 4)//oscar
            startActivity(intent)
        }

        adapterNowShowing.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItem
            startActivity(
                Intent(context, DetailMovieActivity::class.java)
                    .putExtra("MOVIE_ID", myItem.movie.id)
                    .putExtra("MOVIE_POSTER", myItem.movie.poster_path)
                    .putExtra("MOVIE_BACKDROP", myItem.movie.backdrop_path)
                    .putExtra("MOVIE_VOTE", myItem.movie.vote_average)
                    .putExtra("MOVIE_DATE", myItem.movie.release_date)
                    .putExtra("MOVIE_TITLE", myItem.movie.title)
            )
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
            val myItem = item as MovieItemSmall
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

        adapterTopRated.setOnItemClickListener { item, _ ->
            val myItem = item as MovieItemSmall
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

        listMovieNowShowing =
            activity?.intent?.getSerializableExtra("listMovieNowShowing") as ArrayList<Movie>
        listMovieUpComing =
            activity?.intent?.getSerializableExtra("listMovieUpComing") as ArrayList<Movie>

        listMovieUpComing.sortWith(compareBy { it.release_date })

        val slider = view.findViewById<Slider>(R.id.image_slider)
        val slideList = ArrayList<Slide>()
        for (m in listMovieNowShowing) {
            slideList.add(
                Slide(
                    m.id,
                    "https://image.tmdb.org/t/p/original/${m.backdrop_path.toString()}",
                    0
                )
            )
        }
        slider.addSlides(slideList)

        slider.setItemClickListener { _, _, i, _ ->
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

        adapterNowShowing.clear()
        for (m in listMovieNowShowing) {
            if (m.poster_path != null) adapterNowShowing.add(MovieItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_nowshowing).adapter = adapterNowShowing

        adapterUpComing.clear()
        for (m in listMovieUpComing) {
            if (m.poster_path != null) adapterUpComing.add(MovieItem(m))
        }
        view.findViewById<RecyclerView>(R.id.fm_list_upcoming).adapter = adapterUpComing

        fetchPopular(view)
        fetchTopRated(view)
    }

    private fun fetchPopular(view: View) {
        view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = VISIBLE
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/popular?api_key=d4a7514dbdd976453d2679e036009283&language=vi&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMoviePopular.clear()
                listMoviePopular = result.results

                activity?.runOnUiThread {
                    adapterPopular.clear()
                    for (m in listMoviePopular) {
                        adapterPopular.add(MovieItemSmall(m))
                    }
                    fm_list_popular.adapter = adapterPopular
                    view.findViewById<ProgressBar>(R.id.fm_loading_popular).visibility = GONE
                }
            }
        })
    }

    private fun fetchTopRated(view: View) {
        view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = VISIBLE
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/movie/top_rated?api_key=d4a7514dbdd976453d2679e036009283&language=vi&region=US")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, Result::class.java)

                listMovieTopRated.clear()
                listMovieTopRated = result.results

                activity?.runOnUiThread {
                    adapterTopRated.clear()
                    for (m in listMovieTopRated) {
                        adapterTopRated.add(MovieItemSmall(m))
                    }
                    fm_list_toprated.adapter = adapterTopRated
                    view.findViewById<ProgressBar>(R.id.fm_loading_toprated).visibility = GONE
                }
            }
        })
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

class Result(val results: ArrayList<Movie>)

class MovieItem(val movie: Movie) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + movie.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.m_poster)
            }

            viewHolder.itemView.m_title.text = movie.title
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class MovieItemSmall(val movie: Movie) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_small
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.m_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + movie.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.m_poster)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

//class MovieItemUpComing(val movie: Movie) : Item<ViewHolder>() {
//    override fun getLayout(): Int {
//        return R.layout.movie_item_with_date
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    private fun daysBetween(releaseDate: String): Int {
//        val now = System.currentTimeMillis()
//
//        val day = releaseDate.substring(8, 10).toInt()
//        val month = releaseDate.substring(5, 7).toInt()
//        val year = releaseDate.substring(0, 4).toInt()
//
//        val stringDate = "$year/$month/$day"
//        val date = SimpleDateFormat("yyyy/MM/dd").parse(stringDate) as Date
//        val time = date.time
//
//        val diff = (time - now) / (1000 * 60 * 60 * 24)
//        return diff.toInt()
//    }
//
//    override fun bind(viewHolder: ViewHolder, position: Int) {
//        try {
//            if (movie.poster_path == null) {
//                viewHolder.itemView.m2_poster.setImageResource(R.drawable.logo_blue)
//            } else {
//                Picasso.get()
//                    .load("https://image.tmdb.org/t/p/w300" + movie.poster_path)
//                    .placeholder(R.drawable.logo_accent)
//                    .fit()
//                    .into(viewHolder.itemView.m2_poster)
//            }
//
//            viewHolder.itemView.m2_date_left.text =
//                "${daysBetween(movie.release_date!!) + 1} ngày nữa"
//
//            viewHolder.itemView.m2_title.text = movie.title
//        } catch (e: Exception) {
//            Log.d("error_here", e.toString())
//        }
//    }
//}

class MovieItemRow(val movie: Movie) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (movie.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w200" + movie.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.mrow_poster)
            }

            viewHolder.itemView.mrow_title.text = movie.title
            viewHolder.itemView.mrow_description.text = movie.overview
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class ShowItemRow(val show: Show) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.movie_item_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (show.poster_path == null) {
                viewHolder.itemView.mrow_poster.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + show.poster_path)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(viewHolder.itemView.mrow_poster)
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
