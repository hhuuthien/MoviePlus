package com.thien.movieplus

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.android.synthetic.main.list_create_layout.view.*

class FragmentAccountList : Fragment() {

    private var listList = ArrayList<UserList>()
    private val adapterList = GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_list, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.findViewById<RecyclerView>(R.id.fal_list).layoutManager = layoutManager

        adapterList.setOnItemClickListener { item, _ ->
            val myItem = item as ListItem
            startActivity(
                Intent(context, ListActivity::class.java)
                    .putExtra("type", 101)
                    .putExtra("listID", myItem.list.id)
                    .putExtra("listName", myItem.list.name)
            )
        }

        adapterList.setOnItemLongClickListener { item, _ ->
            val myItem = item as ListItem
            val dialog = AlertDialog.Builder(context)
                .setMessage("Chọn hành động")
                .setPositiveButton("Xoá") { _, _ ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val database = FirebaseDatabase.getInstance()
                    val ref = database
                        .getReference(currentUser!!.uid).child("list").child(myItem.list.id)
                    ref.removeValue().addOnCompleteListener {
                        Toast.makeText(
                            context,
                            "Đã xoá list ${myItem.list.name}",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                .setNegativeButton("Đổi tên") { _, _ ->
                    val myLayout = layoutInflater.inflate(R.layout.list_create_layout, null)
                    myLayout.listcreate_name.setText(myItem.list.name)
                    myLayout.listcreate_name.hint = "Nhập tên mới"
                    myLayout.listcreate_ok.text = "Đổi tên"
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(context!!)
                        .setView(myLayout)
                        .create()
                    dialog.show()

                    myLayout.listcreate_ok.setOnClickListener {
                        val user = FirebaseAuth.getInstance().currentUser
                        val database = FirebaseDatabase.getInstance()

                        val name = myLayout.listcreate_name.text.toString().trim()
                        val ref =
                            database.getReference(user!!.uid).child("list").child(myItem.list.id)
                                .child("name")
                        ref.setValue(name).addOnSuccessListener {
                            Toast.makeText(context, "Đã đổi tên", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }
                }.setNeutralButton("Huỷ bỏ") { _, _ ->
                    return@setNeutralButton
                }
                .setCancelable(true)
                .create()
            dialog.show()
            false
        }

        fetchList(view)
    }

    private fun fetchList(view: View) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val data = FirebaseDatabase.getInstance()
            val ref = data.getReference(currentUser!!.uid).child("list")

            val listener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    listList.clear()
                    adapterList.clear()

                    for (p in p0.children) {
                        val string = p.value.toString()
                        val listName = string.substringAfterLast("name=").substringBefore(", id=")
                        val listId = string.substringAfterLast("id=").substringBefore("}")
                        val userList = UserList(listId, listName)
                        listList.add(userList)
                        adapterList.add(ListItem(userList))
                        adapterList.notifyDataSetChanged()
                    }
                }
            }
            ref.addValueEventListener(listener)
            view.findViewById<RecyclerView>(R.id.fal_list).adapter = adapterList
            adapterList.notifyDataSetChanged()
        } catch (e:Exception) {
            //exception
        }
    }
}