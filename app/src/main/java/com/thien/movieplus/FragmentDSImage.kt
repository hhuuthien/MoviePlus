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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.download_layout.view.*
import kotlinx.android.synthetic.main.fragment_ds_image.*
import okhttp3.*
import java.io.IOException

class FragmentDSImage : Fragment() {

    private var listBackdrop = ArrayList<Image>()
    private var listPoster = ArrayList<Image>()
    private val adapterImage = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ds_image, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val showId = arguments?.getInt("s_id", -1)
        if (showId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(showId.toString(),view)
        }

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        view.findViewById<RecyclerView>(R.id.ds_list_image).layoutManager = layoutManager

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

    private fun fetch(showId: String, view: View) {
        view.findViewById<ProgressBar>(R.id.dm_loading_7).visibility = View.VISIBLE
        val url =
            "https://api.themoviedb.org/3/tv/$showId/images?api_key=d4a7514dbdd976453d2679e036009283&language=en"
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

                    ds_list_image.adapter = adapterImage
                    view.findViewById<ProgressBar>(R.id.dm_loading_7).visibility = View.GONE
                }
            }
        })
    }
}