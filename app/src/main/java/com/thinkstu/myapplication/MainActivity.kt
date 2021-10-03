package com.thinkstu.myapplication


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MenuDialogBuilder
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
        val items = arrayOf("小营校区", "健翔桥校区", "清河校区")
val a=StringBuilder()
        //初始化SharedPreferences
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs=getPreferences(MODE_PRIVATE)

        //初始化校区选择器
        xq_selector.text=items[prefs.getInt("campus",0)]

        //默认选择allDay与today
        today.isChecked=true
        allDay.isChecked=true
        morning.isChecked = true
        afternoon.isChecked = true
        night.isChecked = true

        //四个CheckButton的选择事件,复用了之前写的javafx代码
        allDay.setOnClickListener {
            if (allDay.isChecked) {
                morning.isChecked = true
                afternoon.isChecked = true
                night.isChecked = true
            } else {
                morning.isChecked = false
                afternoon.isChecked = false
                night.isChecked = false
            }

        }
        morning.setOnClickListener {
            if (morning.isChecked && afternoon.isChecked && night.isChecked) allDay.isChecked =
                true else if (!morning.isChecked && afternoon.isChecked && night.isChecked
                && allDay.isChecked
            ) {
                morning.isChecked = true
                allDay.isChecked = false
                afternoon.isChecked = false
                night.isChecked = false
            } else if (morning.isChecked) {
                afternoon.isChecked = false
                night.isChecked = false
            } else if (!morning.isChecked) {
                allDay.isChecked = false
                afternoon.isChecked = false
                night.isChecked = false
            } else allDay.isChecked = false
        }
        afternoon.setOnClickListener {
            if (morning.isChecked && afternoon.isChecked && night.isChecked) allDay.isChecked =
                true else if (!afternoon.isChecked && morning.isChecked && night.isChecked
                && allDay.isChecked
            ) {
                afternoon.isChecked = true
                allDay.isChecked = false
                morning.isChecked = false
                night.isChecked = false
            } else if (afternoon.isChecked) {
                morning.isChecked = false
                night.isChecked = false
            } else if (!afternoon.isChecked) {
                allDay.isChecked = false
                morning.isChecked = false
                night.isChecked = false
            } else allDay.isChecked = false
        }
        night.setOnClickListener {
            if (morning.isChecked && afternoon.isChecked && night.isChecked) allDay.isChecked =
                true else if (!night.isChecked && afternoon.isChecked && morning.isChecked
                && allDay.isChecked
            ) {
                night.isChecked = true
                morning.isChecked = false
                afternoon.isChecked = false
                allDay.isChecked = false
            } else if (night.isChecked) {
                morning.isChecked = false
                afternoon.isChecked = false
            } else if (!night.isChecked) {
                allDay.isChecked = false
                afternoon.isChecked = false
                morning.isChecked = false
            } else allDay.isChecked = false
        }

        //校区选择器
        xq_selector.setOnClickListener {

            MenuDialogBuilder(this)
                .addItems(items) { dialog, which ->
                    Toast.makeText(this, "你选择了 " + items[which], Toast.LENGTH_SHORT).show()
                    xq_selector.text = items[which]
                    editor.putInt("campus",which)
                    editor.apply()
                    dialog.dismiss()
                }
                .show()
        }

        //查询按钮,1.5s
        btSearch.setOnClickListener {
            val loadGo =
                QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("正在加载")
                    .create(true)
            val loadSuccess =
                QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                    .setTipWord("查询成功")
                    .create(true)
            loadGo.show()
            btSearch.postDelayed(Runnable {
                loadGo.dismiss()
                //跳出请求成功的Tip
                loadSuccess.show()
            }, 500)
            btSearch.postDelayed(Runnable { loadSuccess.dismiss() }, 1500)

//
        }

    }

    override fun onDestroy() {
        super.onDestroy()
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