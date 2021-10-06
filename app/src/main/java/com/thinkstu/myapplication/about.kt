package com.thinkstu.myapplication

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*

class about : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        back.setOnClickListener {
            this.finish()
        }
        github.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text="https://github.com/Bistutu/emptyClassroom"
            Toast.makeText(this,"已复制github开源地址",Toast.LENGTH_LONG).show()
        }
        wx.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text="Bistutu"
            Toast.makeText(this,"已复制“Bistutu”",Toast.LENGTH_LONG).show()
        }
        contact.setOnClickListener {
            Toast.makeText(this,"作者打烊了~",Toast.LENGTH_LONG).show()
        }

    }
}