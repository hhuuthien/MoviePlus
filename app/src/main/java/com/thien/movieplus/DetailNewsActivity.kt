package com.thien.movieplus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_detail_news.*
import org.jsoup.Jsoup
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter

class DetailNewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_news)

        val title = intent.getStringExtra("NEWS_TITLE")
        dene_title.text = title

        val queue = Volley.newRequestQueue(this)
        val url = intent.getStringExtra("NEWS_LINK")
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val document = Jsoup.parse(response)
                val elements = document.select("div.post-content")
//                dene_content.text = Html.fromHtml(
//                    elements[0].toString(),
//                    Html.FROM_HTML_MODE_LEGACY
//                )
                dene_content.setHtml(
                    elements[0].toString(),
                    HtmlHttpImageGetter(dene_content, null, true)
                )
            },
            Response.ErrorListener {
            })
        queue.add(stringRequest)
    }
}
