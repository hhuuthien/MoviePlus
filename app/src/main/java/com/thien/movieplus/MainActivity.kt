package com.thien.movieplus

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val movieFragment = MovieFragment()
    private val showFragment = ShowFragment()
    private val searchFragment = SearchFragment()
    private val listFragment = ListFragment()

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_movie -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_fragment_container, movieFragment).commit()
                m_toolbar.title = "Phim"
            }
            R.id.nav_show -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_fragment_container, showFragment).commit()
                m_toolbar.title = "TV show"
            }
            R.id.nav_search -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_fragment_container, searchFragment).commit()
                m_toolbar.title = "Tìm kiếm"
            }
            R.id.nav_list -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_fragment_container, listFragment).commit()
                m_toolbar.title = "Danh sách phổ biến"
            }
            R.id.nav_login -> {
                startActivity(Intent(this, AccountActivity::class.java))
            }
        }
        m_layout_drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(m_toolbar)
        val toggle = ActionBarDrawerToggle(
            this, m_layout_drawer, m_toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        m_layout_drawer.addDrawerListener(toggle)
        toggle.syncState()

        m_nav_view.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.m_fragment_container, movieFragment).commit()
            m_nav_view.setCheckedItem(R.id.nav_movie)
            m_toolbar.title = "Phim"
        }
    }

    override fun onBackPressed() {
        if (m_layout_drawer.isDrawerOpen(GravityCompat.START)) {
            m_layout_drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                val dialog = AlertDialog.Builder(this)
                    .setMessage("Ứng dụng sẽ đóng lại sau quá trình làm mới")
                    .setPositiveButton("Làm mới") { _, _ ->
                        (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }
                    .setNegativeButton("Huỷ bỏ") { _, _ ->

                    }
                    .setCancelable(true)
                    .create()
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
