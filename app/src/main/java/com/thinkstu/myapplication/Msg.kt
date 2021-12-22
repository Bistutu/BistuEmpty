package com.thinkstu.myapplication

import android.content.Context
import android.widget.Toast

object Msg {
    fun short(context: Context, messages:String){
        Toast.makeText(context,messages,Toast.LENGTH_SHORT).show()
    }
    fun long(context: Context, messages:String){
        Toast.makeText(context,messages,Toast.LENGTH_LONG).show()
    }
}