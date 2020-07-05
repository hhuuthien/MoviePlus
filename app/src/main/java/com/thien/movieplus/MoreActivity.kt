package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_more.*
import kotlinx.android.synthetic.main.network_item.view.*

class MoreActivity : AppCompatActivity() {

    private val listNetWork = ArrayList<TVNetwork>()
    private val listCompany = ArrayList<TVNetwork>()
    private val adapter = GroupAdapter<ViewHolder>()
    private val adapter2 = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        card1.setOnClickListener {
            startActivity(Intent(this, DiscoverMovieActivity::class.java))
        }

        card2.setOnClickListener {
            startActivity(Intent(this, DiscoverShowActivity::class.java))
        }

        listNetWork.add(TVNetwork(213, "Netflix", "/wwemzKWzjKYJFfCeiB57q3r4Bcm.png"))
        listNetWork.add(TVNetwork(49, "HBO", "/tuomPhY2UtuPTqqFnKMVHvSb724.png"))
        listNetWork.add(TVNetwork(2, "ABC", "/ndAvF4JLsliGreX87jAc9GdjmJY.png"))
        listNetWork.add(TVNetwork(19, "FOX", "/1DSpHrWyOORkL9N2QHX7Adt31mQ.png"))
        listNetWork.add(TVNetwork(56, "Cartoon Network", "/c5OC6oVCg6QP4eqzW6XIq17CQjI.png"))
        listNetWork.add(TVNetwork(6, "NBC", "/o3OedEP0f9mfZr33jz2BfXOUK5.png"))
        listNetWork.add(TVNetwork(16, "CBS", "/nm8d7P7MJNiBLdgIzUK0gkuEA4r.png"))
        listNetWork.add(TVNetwork(54, "Disney Channel", "/rxhJszKVAvAZYTmF0vfRHfyo4Fb.png"))
        listNetWork.add(TVNetwork(67, "Showtime", "/Allse9kbjiP6ExaQrnSpIhkurEi.png"))
        listNetWork.add(TVNetwork(2739, "Disney+", "/gJ8VX6JSu3ciXHuC2dDGAo2lvwM.png"))
        listNetWork.add(TVNetwork(174, "AMC", "/y2hQXpvXOP2PL4XDgjDEG6lNvMc.png"))
        listNetWork.add(TVNetwork(71, "The CW", "/ge9hzeaU7nMtQ4PjkFlc68dGAJ9.png"))
        listNetWork.add(TVNetwork(21, "The WB", "/9GlDHjQj9c2dkfARCR3zlH87R66.png"))
        listNetWork.add(TVNetwork(1, "Fuji TV", "/yS5UJjsSdZXML0YikWTYYHLPKhQ.png"))
        listNetWork.add(TVNetwork(4, "BBC One", "/qTj9SiOJITGUm240kM6e0ZFSJGz.png"))
        listNetWork.add(TVNetwork(1024, "Amazon", "/uK6yuqMkUvKhCgVJjg5JWDUoabA.png"))
        listNetWork.shuffle()

        for (m in listNetWork) {
            adapter.add(NetworkItem(m))
        }
        network_list.adapter = adapter
        adapter.setOnItemClickListener { item, _ ->
            val myitem = item as NetworkItem
            val intent = Intent(this, List2ActivityForShow::class.java)
            intent.putExtra("NETWORK_ID", myitem.network.id)
            intent.putExtra("NETWORK_NAME", myitem.network.name)
            intent.putExtra("NETWORK_IMAGE", myitem.network.image)
            intent.putExtra("TYPE", "show")
            startActivity(intent)
        }

        network_list.layoutManager =
            GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)

        listCompany.add(TVNetwork(1, "Lucasfilm", "/o86DbpburjxrqAzEDhXZcyE8pDb.png"))
        listCompany.add(TVNetwork(2, "Walt Disney Pictures", "/wdrCwmRnLFJhEoH8GSfymY85KHT.png"))
        listCompany.add(TVNetwork(3, "Pixar", "/1TjvGVDMYsj6JBxOAkUHpPEwLf7.png"))
        listCompany.add(TVNetwork(4, "Paramount", "/fycMZt242LVjagMByZOLUGbCvv3.png"))
        listCompany.add(TVNetwork(5, "Columbia Pictures", "/71BqEFAF4V3qjjMPCpLuyJFB9A.png"))
        listCompany.add(TVNetwork(7, "DreamWorks Pictures", "/vru2SssLX3FPhnKZGtYw00pVIS9.png"))
        listCompany.add(TVNetwork(12, "New Line Cinema", "/cYR2YbturmNkU77MXa9gHIbuuAe.png"))
        listCompany.add(
            TVNetwork(
                17,
                "Warner Bros. Entertainment",
                "/s1y7CTv6YHe87YUGOq6SRB6DmO7.png"
            )
        )
        listCompany.add(TVNetwork(25, "20th Century Fox", "/qZCc1lty5FzX30aOCVRBLzaVmcp.png"))
        listCompany.add(TVNetwork(33, "Universal Pictures", "/8lvHyhjr8oUKOOy2dKXoALWKdp0.png"))
        listCompany.add(TVNetwork(34, "Sony Pictures", "/GagSvqWlyPdkFHMfQ3pNq6ix9P.png"))
        listCompany.add(
            TVNetwork(
                43,
                "Fox Searchlight Pictures",
                "/4RgIPr55kBakgupWkzdDxqXJEqr.png"
            )
        )
        listCompany.add(TVNetwork(174, "Warner Bros. Pictures", "/6rFNo5taSC9i0Sxnl81nucQMsw9.png"))
        listCompany.add(
            TVNetwork(
                2785,
                "Warner Bros. Animation",
                "/l5zW8jjmQOCx2dFmvnmbYmqoBmL.png"
            )
        )
        listCompany.add(
            TVNetwork(
                2251,
                "Sony Pictures Animation",
                "/8PUjvTVmtJDdDXURTaSoPID0Boj.png"
            )
        )
        listCompany.add(TVNetwork(420, "Marvel Studios", "/hUzeosd33nzE5MCNsZxCGEKTXaQ.png"))
        listCompany.add(TVNetwork(521, "DreamWorks Animation", "/cHvwSfUAG1KPkiwdBDwtn5ahX0P.png"))
        listCompany.add(TVNetwork(2348, "Nickelodeon Movies", "/m31fQvZJuUvAgxoqTiCGYFBfZYe.png"))
        listCompany.add(TVNetwork(22213, "TSG Entertainment", "/qx9K6bFWJupwde0xQDwOvXkOaL8.png"))
        listCompany.add(
            TVNetwork(
                3172,
                "Blumhouse Productions",
                "/kDedjRZwO8uyFhuHamomOhN6fzG.png"
            )
        )
        listCompany.add(TVNetwork(9993, "DC Entertainment", "/zC29tHZxkdeiVR1IXbrQJJQSuUQ.png"))
        listCompany.add(
            TVNetwork(
                6125,
                "Walt Disney Animation Studios",
                "/tVPmo07IHhBs4HuilrcV0yujsZ9.png"
            )
        )
        listCompany.add(TVNetwork(333, "Original Film", "/5xUJfzPZ8jWJUDzYtIeuPO4qPIa.png"))
        listCompany.add(TVNetwork(10342, "Studio Ghibli", "/uFuxPEZRUcBTEiYIxjHJq62Vr77.png"))
        listCompany.add(TVNetwork(9383, "Blue Sky Studios", "/ppeMh4iZJQUMm1nAjRALeNhWDfU.png"))
        listCompany.add(TVNetwork(5542, "Toei Animation", "/ayE4LIqoAWotavo7xdvYngwqGML.png"))
        listCompany.add(
            TVNetwork(
                6704,
                "Illumination Entertainment",
                "/acf3yqGq8Bm9smHwkQGRDVO5CM5.png"
            )
        )
        listCompany.add(TVNetwork(47729, "STX Entertainment", "/5NRpQ7xxmODXAjt2pRWUFMLVzvP.png"))
        listCompany.add(TVNetwork(1632, "Lionsgate", "/cisLn1YAUuptXVBa0xjq7ST9cH0.png"))
        listCompany.add(
            TVNetwork(
                7076,
                "Chernin Entertainment",
                "/8BFxn9NUWSgp0ndih569Gm62xiC.png"
            )
        )
        listCompany.shuffle()

        for (m in listCompany) {
            adapter2.add(NetworkItem(m))
        }
        company_list.adapter = adapter2
        adapter2.setOnItemClickListener { item, _ ->
            val myitem = item as NetworkItem
            val intent = Intent(this, List2ActivityForMovie::class.java)
            intent.putExtra("NETWORK_ID", myitem.network.id)
            intent.putExtra("NETWORK_NAME", myitem.network.name)
            intent.putExtra("NETWORK_IMAGE", myitem.network.image)
            intent.putExtra("TYPE", "movie")
            startActivity(intent)
        }

        company_list.layoutManager =
            GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
    }
}

class TVNetwork(
    val id: Int,
    val name: String,
    val image: String
)

class NetworkItem(val network: TVNetwork) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.network_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get()
            .load("https://image.tmdb.org/t/p/w300" + network.image)
            .into(viewHolder.itemView.logoView)
    }
}