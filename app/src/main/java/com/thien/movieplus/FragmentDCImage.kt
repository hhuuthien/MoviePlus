package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_dc_image.*
import kotlinx.android.synthetic.main.fragment_dc_info.*
import kotlinx.android.synthetic.main.fragment_dc_movie.*
import kotlinx.android.synthetic.main.fragment_dm_info.*
import kotlinx.android.synthetic.main.product_of_cast_item.view.*
import okhttp3.*
import java.io.IOException

class FragmentDCImage : Fragment() {

    private var listImage = ArrayList<Image>()
    private val adapterImage = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dc_image, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val castId = arguments?.getInt("c_id", -1)
        if (castId == -1) {
            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
        } else {
            fetch(castId.toString())
        }

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        view.findViewById<RecyclerView>(R.id.dc_list_image).layoutManager = layoutManager

        adapterImage.setOnItemClickListener { item, _ ->
            val intent = Intent(context, PictureActivity::class.java)
            val myItem = item as ImageItem
            intent.putExtra("imageString", myItem.image.file_path)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
    }

    private fun fetch(castId: String) {
        val url = "https://api.themoviedb.org/3/person/$castId/images?api_key=d4a7514dbdd976453d2679e036009283"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onFetchingResult", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gSon = GsonBuilder().create()
                val result = gSon.fromJson(body, ImageResult::class.java)

                listImage = result.profiles as ArrayList<Image>

                activity?.runOnUiThread {
                    adapterImage.clear()
                    try {
                        for (m in listImage) {
                            adapterImage.add(ImageItem(m))
                        }
                        dc_list_image.adapter = adapterImage
                    } catch (e:Exception) {
                        Log.d("error_here",e.toString())
                    }
                }
            }
        })
    }
}

class ImageResult(
    val profiles: List<Image>
)
