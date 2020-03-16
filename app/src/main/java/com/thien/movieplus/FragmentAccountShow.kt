package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class FragmentAccountShow : Fragment() {

    private var listShow = ArrayList<Show>()
    private val adapterShow = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_show, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//        val layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        view.findViewById<RecyclerView>(R.id.fas_list).layoutManager = layoutManager

        view.findViewById<TextView>(R.id.fas_text2).visibility = View.VISIBLE
        view.findViewById<RecyclerView>(R.id.fas_list).visibility = View.GONE

        val pref = context!!.getSharedPreferences("SettingPref", 0)
        val english = pref.getBoolean("english", false)
        val goodquality = pref.getBoolean("goodquality", true)

        adapterShow.setOnItemClickListener { item, _ ->
            val intent = Intent(context, DetailShowActivity::class.java)
            val showItem = item as ShowItem
            intent.putExtra("SHOW_ID", showItem.show.id)
            intent.putExtra("SHOW_POSTER", showItem.show.poster_path)
            intent.putExtra("SHOW_TITLE", showItem.show.name)
            intent.putExtra("SHOW_BACKDROP", showItem.show.backdrop_path)
            intent.putExtra("SHOW_VOTE", showItem.show.vote_average)
            startActivity(intent)
        }

        fetchShow(view, goodquality)
    }

    private fun fetchShow(view: View, goodquality: Boolean) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val myRef =
                FirebaseDatabase.getInstance().getReference(currentUser!!.uid).child("love_show")

            val listener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    listShow.clear()
                    adapterShow.clear()

                    for (p in p0.children) {
                        val string = p.value.toString()
                        val showTitle =
                            string.substringAfter("name=").substringBefore(", id=")
                        val showId = string.substringAfter("id=").substringBefore(", poster_path=")
                        val showPoster =
                            string.substringAfter("poster_path=").substringBeforeLast("}")
                        val showBackdrop =
                            string.substringAfter("backdrop_path=").substringBefore(", overview=")
                        val showVote =
                            string.substringAfter("vote_average=").substringBeforeLast(", name=")
                        val show = Show(
                            showPoster,
                            showId.toInt(),
                            showTitle,
                            showVote.toDouble(),
                            showBackdrop,
                            ""
                        )
                        listShow.add(show)
                        adapterShow.add(ShowItem(show, goodquality))
                        adapterShow.notifyDataSetChanged()
                    }

                    if (listShow.size == 0) {
                        view.findViewById<TextView>(R.id.fas_text2).visibility = View.VISIBLE
                        view.findViewById<RecyclerView>(R.id.fas_list).visibility = View.GONE
                    } else {
                        view.findViewById<TextView>(R.id.fas_text2).visibility = View.GONE
                        view.findViewById<RecyclerView>(R.id.fas_list).visibility = View.VISIBLE
                    }
                }
            }
            myRef.addValueEventListener(listener)
            view.findViewById<RecyclerView>(R.id.fas_list).adapter = adapterShow
            adapterShow.notifyDataSetChanged()
        } catch (e: Exception) {
            view.findViewById<TextView>(R.id.fas_text2).visibility = View.VISIBLE
            view.findViewById<RecyclerView>(R.id.fas_list).visibility = View.GONE
        }
    }
}