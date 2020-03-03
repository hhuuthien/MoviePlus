package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.download_layout.view.*
import kotlinx.android.synthetic.main.fragment_dm_image.*
import kotlinx.android.synthetic.main.image_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDMImage : Fragment() {

    private var listBackdrop = ArrayList<Image>()
    private var listPoster = ArrayList<Image>()
    private val adapterImage = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dm_image, container, false)
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

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        view.findViewById<RecyclerView>(R.id.dm_list_image).layoutManager = layoutManager

        adapterImage.setOnItemClickListener { item, _ ->
            val intent = Intent(context, PictureActivity::class.java)
            val myItem = item as ImageItem
            intent.putExtra("imageString", myItem.image.file_path)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        adapterImage.setOnItemLongClickListener { item, _ ->
            val myLayout = layoutInflater.inflate(R.layout.download_layout, null)
            val dialog = AlertDialog.Builder(context!!)
                .setView(myLayout)
                .create()
            myLayout.dummytext.setOnClickListener {
                dialog.dismiss()
                val myItem = item as ImageItem
                val intent = Intent(context, PermissionActivity::class.java)
                    .putExtra("type", "toDownloadImage")
                    .putExtra("imageString", myItem.image.file_path)
                startActivity(intent)
            }
            dialog.show()
            false
        }
    }

    private fun fetch(movieId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_3).visibility = View.VISIBLE
        val url =
            "https://api.themoviedb.org/3/movie/$movieId/images?api_key=d4a7514dbdd976453d2679e036009283&include_image_language=vi,en"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ResultMovieImage::class.java)

                listBackdrop.clear()
                listPoster.clear()
                listBackdrop = result.backdrops as ArrayList<Image>
                listPoster = result.posters as ArrayList<Image>

                val a = listPoster.size
                val b = listBackdrop.size

                activity?.runOnUiThread {
                    adapterImage.clear()

                    when {
                        a < b -> {
                            for (m in 0 until a) {
                                adapterImage.add(ImageItem(listPoster[m]))
                                adapterImage.add(ImageItem(listBackdrop[m]))
                            }
                            for (m in a until b) {
                                adapterImage.add(ImageItem(listBackdrop[m]))
                            }
                        }
                        a > b -> {
                            for (m in 0 until b) {
                                adapterImage.add(ImageItem(listPoster[m]))
                                adapterImage.add(ImageItem(listBackdrop[m]))
                            }
                            for (m in b until a) {
                                adapterImage.add(ImageItem(listPoster[m]))
                            }
                        }
                        else -> {
                            for (m in 0 until a) {
                                adapterImage.add(ImageItem(listPoster[m]))
                                adapterImage.add(ImageItem(listBackdrop[m]))
                            }
                        }
                    }

                    dm_list_image.adapter = adapterImage
                    view.findViewById<ProgressBar>(R.id.dm_loading_3).visibility = View.GONE
                }
            }
        })
    }
}

class ResultMovieImage(
    val backdrops: List<Image>?,
    val posters: List<Image>?
)

class ImageItem(val image: Image) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.image_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        try {
            if (image.file_path == null) {
                viewHolder.itemView.i_image.setImageResource(R.drawable.logo_blue)
            } else {
                Picasso.get()
                    .load("https://image.tmdb.org/t/p/w300" + image.file_path)
                    .placeholder(R.drawable.logo_accent)
                    .into(viewHolder.itemView.i_image)
            }
        } catch (e: Exception) {
            Log.d("error_here", e.toString())
        }
    }
}

class Image(
    val file_path: String?
)