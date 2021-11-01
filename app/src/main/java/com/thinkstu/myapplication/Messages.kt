package com.thinkstu.myapplication

import android.content.Context
import android.widget.Toast

object Messages {
    fun emitShort(context: Context, messages:String){
        Toast.makeText(context,messages,Toast.LENGTH_SHORT).show()
    }
    fun emitLong(context: Context, messages:String){
        Toast.makeText(context,messages,Toast.LENGTH_LONG).show()
    }
}