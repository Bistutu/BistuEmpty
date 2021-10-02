package com.thinkstu.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.line1 -> Toast.makeText(this, "sssss", Toast.LENGTH_LONG).show()
            R.id.line2 -> Toast.makeText(this, "sssss", Toast.LENGTH_LONG).show()
            R.id.line3 -> Toast.makeText(this, "sssss", Toast.LENGTH_LONG).show()
        }
        return true

    }
}