package com.thinkstu.myapplication

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MenuDialogBuilder
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.android.synthetic.main.recycler_view_model.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MessageDialogBuilder

class MainActivity : AppCompatActivity() {
    //全局变量均放在了最末尾
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
        val items = arrayOf("小营校区", "健翔桥校区", "清河校区")
        //初始化SharedPreferences
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs = getPreferences(MODE_PRIVATE)
        //初始化校区选择器
        xq = prefs.getInt("campus", 1) + 1
        xq_selector.text = items[xq - 1]
        //默认选择“今天”  和  “全天”
        defaultSelected()
        //四个CheckButton的选择事件,复用了之前写的javafx代码
        time = fourCheckAction()
        //校区选择器
        xq_selector(items, editor)
        //日期选择器
        date_selector()
        //按下查询按钮,1.5s
        btSearch.setOnClickListener {
            //创建三个提示的dialog
            if (time == -1) {
                Toast.makeText(this, "请选择一个时段~", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val (loadGo, loadSuccess, loadFaile) = triple_dialog()
            loadGo.show()
            //发送网络请求
            thread {
                try {
                    var keyUrl: String = "" + xq + "/" + time + xq + month + day
                    var url: String = "https://bistutu.github.io/demoEmpty/" + keyUrl + ".json"
                    val responseData = okhttp_model.send(url)
                    //跳出请求成功的Dialog

                    if (responseData != null) {
                        when (weekDay) {
                            1 -> weekString = "星期一"
                            2 -> weekString = "星期二"
                            3 -> weekString = "星期三"
                            4 -> weekString = "星期四"
                            5 -> weekString = "星期五"
                            6 -> weekString = "星期六"
                            7 -> weekString = "星期日"
                        }
                        //判断时段
                        var timeString = ""
                        when (time) {
                            0 -> timeString = "全天"
                            1 -> timeString = "上午"
                            2 -> timeString = "下午"
                            3 -> timeString = "晚上"
                        }
                        date.text =
                            "你所查询的日期为：" + month + "月" + day + "日" + "(" + weekString + ")  " + timeString
                        //请求成功后，渲染recyclerview界面
                        showSuccess(responseData, loadGo, loadSuccess, loadFaile)
                    }
                } catch (e: Exception) {
                    //网络请求失败时的操作
                    btSearch.postDelayed(Runnable { loadGo.dismiss();loadFaile.show() }, 500)
                    btSearch.postDelayed(Runnable { loadFaile.dismiss() }, 1500)
                }
            }
        }
        //查询声明Button
        declareButton.setOnClickListener {
            when (xq) {
                1 -> {
                    MessageDialogBuilder(this)
                        .setTitle("小营校区查询的范围为：")
                        .setMessage(
                            "（1）一教阶梯教室\n" +
                                    "（2）二教全部\n" +
                                    "（3）四教全部"
                        )
                        .addAction(
                            "已阅~"
                        ) { dialog, index -> dialog.dismiss() }
                        .show()
                }
                2 -> {
                    MessageDialogBuilder(this)
                        .setTitle("健翔桥校区查询的范围为：")
                        .setMessage(
                            "（1）一教阶梯教室\n" +
                                    "（2）二教全部\n" +
                                    "（3）三教阶梯教室"
                        )
                        .addAction(
                            "已阅~"
                        ) { dialog, index -> dialog.dismiss() }
                        .show()
                }
                3 -> {
                    MessageDialogBuilder(this)
                        .setTitle("清河校区查询的范围为：")
                        .setMessage(
                            "（1）一教全部\n" +
                                    "（2）二教全部\n" +
                                    "（3）三教全部"
                        )
                        .addAction(
                            "已阅~"
                        ) { dialog, index -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }
    /*onCreate()方法结束*/

    //日期选择器，待更改——————————————————tomorrow跳转到了下个月——————————————————————————————————————————————————————
    private fun date_selector() {
        today.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
            //判断星期几
            weekDay = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1
        }
        tomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 1
            weekDay = gregorianCalendar.get(Calendar.DAY_OF_WEEK)
        }
        afterTomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 2
            weekDay = (gregorianCalendar.get(Calendar.DAY_OF_WEEK) + 1) % 7
        }
    }

    //校区选择器，无需更改
    private fun xq_selector(
        items: Array<String>, editor: SharedPreferences.Editor
    ) {
        xq_selector.setOnClickListener {
            MenuDialogBuilder(this)
                .addItems(items) { dialog, which ->
                    Toast.makeText(this, "你选择了 " + items[which], Toast.LENGTH_SHORT).show()
                    xq_selector.text = items[which]
                    xq = which + 1
                    editor.putInt("campus", which)
                    editor.apply()
                    dialog.dismiss()
                }
                .show()
        }
    }

    //四个checkButton事件——————————————————————————————————————————————————————————————————————————————————————————
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
            if (allDay.isChecked)
                time = 0
            else
                time = -1
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
            if (morning.isChecked)
                time = 1
            else
                time = -1
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
            if (afternoon.isChecked)
                time = 2
            else
                time = -1
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
            if (night.isChecked)
                time = 3
            else
                time = -1
        }
        return time
    }

    //”默认选择“
    private fun defaultSelected() {
        today.isChecked = true
        allDay.isChecked = true
        morning.isChecked = true
        afternoon.isChecked = true
        night.isChecked = true
    }

    //recyclerView Adapter
    class emptyListAdapter(val context: Context, val emptyList: List<empty_list>, val xq: Int) :
        RecyclerView.Adapter<emptyListAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardtv1 = view.cardtv1;
            val cardtv2 = view.cardtv2
            val cardtv3 = view.cardtv3;
            val cardtv4 = view.cardtv4
            val mc1 = view.mc1;
            val mc2 = view.mc2
            val mc3 = view.mc3;
            val mc4 = view.mc4

        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): emptyListAdapter.ViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.recycler_view_model, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: emptyListAdapter.ViewHolder, position: Int) {
            val empty = emptyList[position]
            if (xq == 2 || xq == 3) {
                holder.cardtv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F);
                holder.cardtv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F);
                holder.cardtv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F);
                holder.cardtv4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F);
                holder.cardtv1.setPadding(15, 15, 15, 15)
                holder.cardtv2.setPadding(15, 15, 15, 15)
                holder.cardtv3.setPadding(15, 15, 15, 15)
                holder.cardtv4.setPadding(15, 15, 15, 15)
            } else {
                holder.cardtv1.setPadding(5, 5, 5, 5)
                holder.cardtv2.setPadding(5, 5, 5, 5)
                holder.cardtv3.setPadding(5, 5, 5, 5)
                holder.cardtv4.setPadding(5, 5, 5, 5)
            }
            if (empty.em1.equals("1")) {
                holder.mc1.visibility = View.GONE
                holder.mc3.visibility = View.GONE
                holder.mc4.visibility = View.GONE
                holder.cardtv2.text = empty.em2
                holder.cardtv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F);
                holder.mc2.setBackgroundResource(R.color.split)
            } else {
                holder.cardtv1.text = empty.em1;holder.cardtv2.text = empty.em2
                holder.cardtv3.text = empty.em3;holder.cardtv4.text = empty.em4
            }


        }

        override fun getItemCount(): Int = emptyList.size
    }

    //网络请求成功时的操作
    private fun showSuccess(
        responseData: String,
        loadGo: Dialog,
        loadSuccess: Dialog,
        loadFaile: Dialog
    ) {
        runOnUiThread {
            try {

                val list: List<empty_list>
                if (xq == 2)
                    list =
                        mutableListOf(empty_list("1", "阶梯教室", "", ""))
                else
                    list =
                        mutableListOf(empty_list("1", "第一教学楼", "", ""))
                val emptyList_all = gson.fromJson<List<empty_list>>(responseData, typeOf)
                for (i in emptyList_all) {
                    list.add(i)
                }
                btSearch.postDelayed(Runnable { loadGo.dismiss();loadSuccess.show() }, 500)
                btSearch.postDelayed(Runnable { loadSuccess.dismiss();loadFaile.dismiss() }, 1500)
                val listArray = ArrayList<empty_list>(list)
                recyclerView.layoutManager = layoutManager
                val adapter = emptyListAdapter(this, listArray, xq)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                btSearch.postDelayed(Runnable {
                    loadSuccess.dismiss();
                    loadGo.dismiss();loadFaile.dismiss()
                }, 500)
                Toast.makeText(this, "服务器开小差了~", Toast.LENGTH_LONG).show()
            }
        }
    }

    //填充menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //点击menu操作
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

    //三个Dialog，不需要更改
    private fun triple_dialog(): Triple<QMUITipDialog, QMUITipDialog, QMUITipDialog> {
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
        return Triple(loadGo, loadSuccess, loadFaile)
    }

    //变量
    var time = 0    //时段
    var xq = 1      //校区标记

    //获取日期的GregorianCalendar
    val gregorianCalendar = GregorianCalendar()
    var month: Int = gregorianCalendar.get(Calendar.MONTH) + 1
    var day: Int = gregorianCalendar.get(Calendar.DAY_OF_MONTH)

    //判断星期几
    var weekString = ""
    var weekDay: Int = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1

    //Gson
    val gson = Gson()
    val typeOf = object : TypeToken<List<empty_list>>() {}.type
    val layoutManager = LinearLayoutManager(this)

}