package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_main, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {
        view.findViewById<RelativeLayout>(R.id.flm_1).setOnClickListener {
            val intent = Intent(context,ListActivity::class.java)
            intent.putExtra("type",1)//marvel
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_2).setOnClickListener {
            val intent = Intent(context,ListActivity::class.java)
            intent.putExtra("type",2)//vietnam
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_3).setOnClickListener {
            val intent = Intent(context,ListActivity::class.java)
            intent.putExtra("type",3)//pixar
            startActivity(intent)
        }
        view.findViewById<RelativeLayout>(R.id.flm_4).setOnClickListener {
            val intent = Intent(context,ListActivity::class.java)
            intent.putExtra("type",4)//oscar
            startActivity(intent)
        }
    }
}