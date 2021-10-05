package com.thinkstu.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MenuDialogBuilder
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.android.synthetic.main.recycler_view_model.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MessageDialogBuilder


class MainActivity : AppCompatActivity() {
    var time = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
        var xq = 1

        val items = arrayOf("小营校区", "健翔桥校区", "清河校区")
        //初始化SharedPreferences
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs = getPreferences(MODE_PRIVATE)
        //获取日期
        val gregorianCalendar = GregorianCalendar()
        var month: Int = gregorianCalendar.get(Calendar.MONTH) + 1
        var day: Int = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 3

        //初始化校区选择器
        val xq_num = prefs.getInt("campus", 1)
        xq = xq_num + 1
        xq_selector.text = items[prefs.getInt("campus", 1)]
        //默认选择allDay与today
        defaultSelected()
        //四个CheckButton的选择事件,复用了之前写的javafx代码
        time = fourCheckAction()
        //校区选择器
        xq = xq_selector(items, xq, editor)
//日期选择器
        today.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 3
        }
        tomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 4
        }
        afterTomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 5
        }
        //查询按钮,1.5s
        btSearch.setOnClickListener {
            //Start
            val loadGo =
                QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("正在查询")
                    .create(true)
            val loadSuccess =
                QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                    .setTipWord("查询成功")
                    .create(true)
            val loadFaile =
                QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                    .setTipWord("查询失败，请检查网络设置")
                    .create(true)
            loadGo.show()

            //先发送网络请求、线程
            thread {
                try {
                    var keyUrl: String = ((("" + time) + xq) + month) + day
                    var url_formal: String =
                        "https://bistutu.github.io/demoEmpty/" + keyUrl + ".json"
                    val okhttp = OkHttpClient()
                    val request = Request.Builder()
                        .url(url_formal)
                        .build()
                    val response = okhttp.newCall(request).execute()
                    //
                    btSearch.postDelayed(Runnable {
                        //跳出请求成功的Tip
                        loadGo.dismiss()
                        loadSuccess.show()
                    }, 500)
                    val responseData = response.body()?.string()
                    if (responseData != null) {
                        when (time) {
                            0 -> date.text = "你所查询的日期为：" + month + "月" + day + "日全天"
                            1 -> date.text = "你所查询的日期为：" + month + "月" + day + "日上午"
                            2 -> date.text = "你所查询的日期为：" + month + "月" + day + "日下午"
                            3 -> date.text = "你所查询的日期为：" + month + "月" + day + "日晚上"
                        }

                        showSuccess(responseData)
                    }

                } catch (e: Exception) {
                    //判断网络请求：Faile
                    btSearch.postDelayed(Runnable {
                        //跳出请求失败的Tip
                        loadGo.dismiss()
                        loadFaile.show()
                        showFaile()
                    }, 500)
                    e.printStackTrace()
                }
            }

            //最终的延迟处理事件
            btSearch.postDelayed(Runnable {
                loadSuccess.dismiss()
                loadFaile.dismiss()
            }, 2000)
//
        }

    }

    private fun xq_selector(
        items: Array<String>,
        xq: Int,
        editor: SharedPreferences.Editor
    ): Int {
        var xq1 = xq
        xq_selector.setOnClickListener {

            MenuDialogBuilder(this)
                .addItems(items) { dialog, which ->
                    Toast.makeText(this, "你选择了 " + items[which], Toast.LENGTH_SHORT).show()
                    xq_selector.text = items[which]
                    xq1 = which + 1
                    editor.putInt("campus", which)
                    editor.apply()
                    dialog.dismiss()
                }
                .show()
        }
        return xq1
    }

    private fun fourCheckAction(): Int {
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
            time = 0
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
            time = 1
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
            time = 2
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
            time = 3
        }
        return time
    }

    private fun defaultSelected() {
        //默认选择allDay与today
        today.isChecked = true
        allDay.isChecked = true
        morning.isChecked = true
        afternoon.isChecked = true
        night.isChecked = true
    }

    //网络请求失败时的操作
    private fun showFaile() {
        runOnUiThread {
        }
    }

    //Adapter适配器
    class emptyListAdapter(val context: Context, val emptyList: List<empty_list>) :
        RecyclerView.Adapter<emptyListAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardtv1 = view.cardtv1
            val cardtv2 = view.cardtv2
            val cardtv3 = view.cardtv3
            val cardtv4 = view.cardtv4
            /*在这里做判断总会引起不必要的BUG
*               而我现在无法解决，所以为了消灭这个BUG
*               我决定暂时停止这一种写法*/
//            val mc1 = view.mc1
//            val mc2 = view.mc2
//            val mc3 = view.mc3
//            val mc4 = view.mc4
            val mc1 = view.mc1
            val mc2 = view.mc2
            val mc3 = view.mc3
            val mc4 = view.mc4
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): emptyListAdapter.ViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.recycler_view_model, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: emptyListAdapter.ViewHolder, position: Int) {
            val empty = emptyList[position]
            /*在这里做判断总会引起不必要的BUG
            * 而我现在无法解决，所以为了消灭这个BUG
            * 我决定暂时停止这一种写法*/
//            if(empty.em1.equals("block")) {
//                holder.mc1.visibility=View.GONE
//                holder.mc2.setBackgroundResource(R.color.split)
//                holder.mc3.visibility=View.GONE
//                holder.mc4.visibility=View.GONE
//            }
            if(empty.em1.equals("——————————")) {
                holder.mc1.visibility=View.GONE
                holder.mc2.setBackgroundResource(R.color.split)
                holder.mc3.visibility=View.GONE
                holder.mc4.visibility=View.GONE
            }


            holder.cardtv1.text = empty.em1
            holder.cardtv2.text = empty.em2
            holder.cardtv3.text = empty.em3
            holder.cardtv4.text = empty.em4

        }

        override fun getItemCount(): Int = emptyList.size
    }

    //网络请求成功时的操作
    private fun showSuccess(responseData: String) {
        runOnUiThread {
            val list = mutableListOf(empty_list( "——————————", "阶梯教室","——————————", "——————————"))

            //Gson

            val gson = Gson()
            val typeOf = object : TypeToken<List<empty_list>>() {}.type
            val emptyList_all = gson.fromJson<List<empty_list>>(responseData, typeOf)
            for (i in emptyList_all) {
                list.add(i)
            }

            val listArray = ArrayList<empty_list>(list)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager

            val adapter = emptyListAdapter(this, listArray)
            recyclerView.adapter = adapter
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
            R.id.line3 -> {
                val intent = Intent(this, about::class.java)
                startActivity(intent)

            }
            R.id.line2 -> Toast.makeText(this, "已是最新版本！", Toast.LENGTH_LONG).show()
            R.id.line1 -> {
                MessageDialogBuilder(this)
                    .setTitle("使用指南->")
                    .setMessage("\nOne touch.")
                    .addAction(
                        "已阅~"
                    ) { dialog, index -> dialog.dismiss() }
                    .show()
            }
        }
        return true

    }

}