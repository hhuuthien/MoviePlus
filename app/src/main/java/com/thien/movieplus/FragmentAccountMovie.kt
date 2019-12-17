package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class FragmentAccountMovie : Fragment() {

    private var list = ArrayList<Movie>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_movie, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        view.findViewById<RecyclerView>(R.id.fam_list).layoutManager = layoutManager

        adapter.setOnItemClickListener { item, _ ->
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

        fetch(view)
    }

    private fun fetch(view: View) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val myRef =
                FirebaseDatabase.getInstance().getReference(currentUser!!.uid).child("love_movie")

            val listener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    list.clear()
                    adapter.clear()

                    for (p in p0.children) {
                        val movieString = p.value.toString()
                        val movieTitle =
                            movieString.substringAfter("title=").substringBefore(", poster_path=")
                        val movieId = movieString.substringAfter("id=").substringBefore(", title=")
                        val moviePoster =
                            movieString.substringAfter("poster_path=").substringBeforeLast("}")
                        val movieBackdrop =
                            movieString.substringAfter("backdrop_path=").substringBefore(", overview=")
                        val movieDate =
                            movieString.substringAfter("release_date=")
                                .substringBefore(", vote_average=")
                        val movieVote =
                            movieString.substringAfter("vote_average=").substringBefore(", id=")
                        val movie = Movie(
                            moviePoster,
                            movieBackdrop,
                            movieId.toInt(),
                            movieTitle,
                            movieDate,
                            movieVote.toDouble(),
                            ""
                        )
                        list.add(movie)
                        adapter.add(MovieItem(movie))
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            myRef.addValueEventListener(listener)
            view.findViewById<RecyclerView>(R.id.fam_list).adapter = adapter
            adapter.notifyDataSetChanged()
        } catch (e:Exception) {
            //exception
        }
    }
}