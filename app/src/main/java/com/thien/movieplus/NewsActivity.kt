package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.news_item.view.*
import org.jsoup.Jsoup

class NewsActivity : AppCompatActivity() {

    private val list = ArrayList<Newsss>()
    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        supportActionBar?.title = "Tin điện ảnh"

        list_news.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        val queue = Volley.newRequestQueue(this)
        val url = "https://moveek.com/tin-tuc/"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val document = Jsoup.parse(response)
                val elements = document.select("div.card-body div.row")
                for (e in elements) {
                    val article = e.toString()
                    val title =
                        article.substringAfter("class=\"name text-dark\">").substringBefore("</a>")
                            .trim()
//                    val content =
//                        article.substringAfter("d-sm-block\">").substringBefore("</p>").trim()
                    val link =
                        "https://moveek.com" + article.substringAfter("a href=\"").substringBefore("/\" class")
                    val image = article.substringAfter("1x, ").substringBefore(" 2x")
                    val newsss = Newsss(title, "", image, link)
                    list.add(newsss)
                }
//                list.shuffle()
                for (m in list) {
                    adapter.add(NewsItem(m))
                }
                list_news.adapter = adapter
            },
            Response.ErrorListener {
            })
        queue.add(stringRequest)

        adapter.setOnItemClickListener { item, _ ->
            val myItem = item as NewsItem
            val intent = Intent(this, DetailNewsActivity::class.java)
            intent.putExtra("NEWS_TITLE", myItem.newsss.title)
            intent.putExtra("NEWS_LINK", myItem.newsss.link)
            startActivity(intent)
        }
    }
}

class Newsss(
    val title: String,
    val content: String,
    val image: String,
    val link: String
)

class NewsItem(val newsss: Newsss) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.news_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.news_title.text = newsss.title

        Picasso.get()
            .load(newsss.image)
            .into(viewHolder.itemView.news_image)
    }
}