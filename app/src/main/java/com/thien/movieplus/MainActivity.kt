package com.thien.movieplus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mainFragment = MainFragment()

    private lateinit var auth: FirebaseAuth

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_movie -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_fragment_container, mainFragment).commit()
            }
            R.id.nav_login -> {
                startActivity(Intent(this, AccountActivity::class.java))
            }
            R.id.nav_info -> {
                startActivity(Intent(this, InfoActivity::class.java))
            }
            R.id.nav_cine -> {
                startActivity(Intent(this, CinemaActivity::class.java))
            }
            R.id.nav_setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }
            R.id.nav_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
            R.id.nav_discover -> {
                startActivity(Intent(this, MoreActivity::class.java))
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
                .replace(R.id.m_fragment_container, mainFragment).commit()
            m_nav_view.setCheckedItem(R.id.nav_movie)
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
            R.id.menu_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
            R.id.menu_search_voice -> {
                askSpeechInput()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            val errorString = "Thiết bị của bạn không hỗ trợ tìm kiếm bằng giọng nói"
            Toast.makeText(this, errorString, Toast.LENGTH_LONG).show()
        } else {
            //ok -> call dialog
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bạn muốn tìm gì?")
            startActivityForResult(intent, 11)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val key = results!![0].toString()
            startActivity(
                Intent(this, SearchActivity::class.java)
                    .putExtra("voice_key", key)
            )
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val n = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        }
        return false
    }

    override fun onResume() {
        super.onResume()
//        if (!isNetworkConnected()) {
//            startActivity(Intent(this, OoopsActivity::class.java))
//        }

        auth = FirebaseAuth.getInstance()

        val navigationView = findViewById<NavigationView>(R.id.m_nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val appName = headerView.findViewById(R.id.nh_appname) as TextView
        val name = headerView.findViewById(R.id.nh_name) as TextView
        val email = headerView.findViewById(R.id.nh_email) as TextView
        val image = headerView.findViewById(R.id.nh_image) as ImageView

        if (auth.currentUser != null) {
            name.visibility = VISIBLE
            email.visibility = VISIBLE
            appName.visibility = GONE
            try {
                fetchImage()
                fetchInfo()
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
        } else {
            name.visibility = GONE
            email.visibility = GONE
            appName.visibility = VISIBLE
            image.setImageResource(R.drawable.logo_accent)
        }
    }

    private fun fetchInfo() {
        val ref =
            FirebaseDatabase.getInstance()
                .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_name")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val name = p0.value.toString()

                val navigationView = findViewById<NavigationView>(R.id.m_nav_view)
                val headerView: View = navigationView.getHeaderView(0)
                val nameText = headerView.findViewById(R.id.nh_name) as TextView
                val emailText = headerView.findViewById(R.id.nh_email) as TextView

                if (name == "null" || name == "") {
                    nameText.text = "Xin chào!"
                    emailText.text = FirebaseAuth.getInstance().currentUser!!.email
                } else {
                    nameText.text = name
                    emailText.text = FirebaseAuth.getInstance().currentUser!!.email
                }
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun fetchImage() {
        val ref =
            FirebaseDatabase.getInstance()
                .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_image")
        val listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val link = p0.value.toString()

                val navigationView = findViewById<NavigationView>(R.id.m_nav_view)
                val headerView: View = navigationView.getHeaderView(0)
                val imageView = headerView.findViewById(R.id.nh_image) as ImageView

                if (link == "null" || link == "") {
                    imageView.setImageResource(R.drawable.logo_accent)
                } else {
                    Picasso.get()
                        .load(link)
                        .fit()
                        .into(imageView)
                }
            }
        }
        ref.addValueEventListener(listener)
    }
}