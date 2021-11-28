package com.thinkstu.myapplication

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_view_model.view.*

class emptyListAdapter(val context: Context, val emptyList: List<empty_list>, val xq: Int) :
    RecyclerView.Adapter<emptyListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardtv1 = view.cardtv1
        val cardtv2 = view.cardtv2
        val cardtv3 = view.cardtv3
        val cardtv4 = view.cardtv4
        val mc1 = view.mc1
        val mc2 = view.mc2
        val mc3 = view.mc3
        val mc4 = view.mc4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): emptyListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recycler_view_model, parent, false)
        return emptyListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: emptyListAdapter.ViewHolder, position: Int) {
        val empty = emptyList[position]
        //recyclerView中”蓝“条的产生
        if (empty.a.equals("1")||empty.d.equals("11")) {
            holder.mc1.visibility = View.GONE
            holder.mc3.visibility = View.GONE
            holder.mc4.visibility = View.GONE
            holder.cardtv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        }
        when(empty.c){
            "0"->holder.mc2.setBackgroundResource(R.color.split0  )
            "1"->holder.mc2.setBackgroundResource(R.color.split1  )
            "2"->holder.mc2.setBackgroundResource(R.color.split2  )
        }

        if(empty.d.equals("10")){
            holder.mc3.visibility = View.GONE
            holder.mc4.visibility = View.GONE
        }
            holder.cardtv1.text = empty.a;holder.cardtv2.text = empty.b
            holder.cardtv3.text = empty.c;holder.cardtv4.text = empty.d

    }

    override fun getItemCount(): Int = emptyList.size
}