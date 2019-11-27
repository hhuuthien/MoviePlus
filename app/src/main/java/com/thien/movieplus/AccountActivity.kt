package com.thien.movieplus

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.list_create_layout.view.*

class AccountActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.acc_navigation_movie -> acc_view_pager.currentItem = 0
            R.id.acc_navigation_show -> acc_view_pager.currentItem = 1
            R.id.acc_navigation_list -> acc_view_pager.currentItem = 2
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        supportActionBar?.title = "Trang cá nhân"

        acc_navigation.setOnNavigationItemSelectedListener(this)

        val adapter = PagerAdapter(supportFragmentManager)
        adapter.addFrag(FragmentAccountMovie())
        adapter.addFrag(FragmentAccountShow())
        adapter.addFrag(FragmentAccountList())
        acc_view_pager.adapter = adapter

        acc_view_pager.currentItem = 0
        acc_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> acc_navigation.selectedItemId = R.id.acc_navigation_movie
                    1 -> acc_navigation.selectedItemId = R.id.acc_navigation_show
                    2 -> acc_navigation.selectedItemId = R.id.acc_navigation_list
                }
            }
        })

        acc_info_image.setOnClickListener {
            startActivity(
                Intent(this, PermissionActivity::class.java)
                    .putExtra("type", "toChangeImage")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val dialog = AlertDialog.Builder(this)
                .setMessage("Bạn cần đăng nhập để xem trang cá nhân")
                .setPositiveButton("Đăng nhập") { _, _ ->
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                .setNegativeButton("Huỷ bỏ") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .create()
            dialog.show()

            acc_info_layout.visibility = GONE
        } else {
            acc_info_layout.visibility = VISIBLE

            try {
                fetchImage()
                fetchInfo()
                acc_info_email.text = FirebaseAuth.getInstance().currentUser!!.email
            } catch (e: Exception) {
                Log.d("error", e.toString())
            }
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
                if (name == "null" || name == "") {
                    acc_info_name.text = "Xin chào!"
                } else {
                    acc_info_name.text = name
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
                Picasso.get()
                    .load(link)
                    .placeholder(R.drawable.logo_accent)
                    .fit()
                    .into(acc_info_image)
            }
        }
        ref.addValueEventListener(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu_account, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_signout -> {
                auth.signOut()
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_LONG).show()
                finish()
            }
            R.id.menu_change_name -> {
                changeName()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeName() {
        val myLayout = layoutInflater.inflate(R.layout.list_create_layout, null)
        myLayout.listcreate_name.setText("")
        myLayout.listcreate_name.hint = "Nhập tên mới"
        myLayout.listcreate_ok.text = "Đổi tên"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()

        myLayout.listcreate_ok.setOnClickListener {
            val newName = myLayout.listcreate_name.text.toString().trim()
            val ref =
                FirebaseDatabase.getInstance()
                    .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_name")
            ref.setValue(newName).addOnSuccessListener {
                Toast.makeText(this, "Đã đổi tên", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        }
    }
}
