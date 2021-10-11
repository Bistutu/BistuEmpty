package com.thinkstu.myapplication

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
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
        //onCreate()首先发起okhttp请求，判断软件是否需要更新
        thread {
            try {
                val isUpdateJSON =
                    okhttp_model.send("https://bistutu.github.io/BistutuApk/update.json")
                val updateData = gson.fromJson(isUpdateJSON, updateObject::class.java)
                //获取用户的现在软件版本号
                val manager = this.getPackageManager();
                val versionNumber =
                    manager.getPackageInfo(this.getPackageName(), 0).versionName + ""
                //与最新版本号对比，是否需要更新？
                if (!updateData.isUpdate.equals(versionNumber)) {
                    updateDialog(updateData, 1)
                }
            } catch (e: Exception) {
                //不执行任何操作
            }
        }
        //SharedPreferences，这里用来设置每次打开软件时默认选择的校区
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs = getPreferences(MODE_PRIVATE)
        //第一次打开软件时默认选择的校区是”小营校区“，xq是（校区）的缩写
        xq = prefs.getInt("campus", 1) + 1
        xq_selector.text = items[xq - 1]
        //xy_selector默认选择
        xy_select = prefs.getInt("xy_select", 0)
        //xy的教学楼选择方法
        xyMethod(editor)

        //这是一个方法，我放在了最后面。每次打开软件都会默认选择 “今天”  和  “全天”
        defaultSelected()
        //四个CheckButton的选择事件
        time = fourCheckAction()
        //校区选择Button事件
        xq_selector(items, editor)
        //日期选择事件
        date_selector()

        //按下查询按钮
        btSearch.setOnClickListener {
            //如果用户没有选择一个时段（time），这里就会发出一个警告
            if (time == -1) {
                Toast.makeText(this, "请选择一个时段~", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            /*创建三个提示的dialog，以待备用
            * 分别是：loadGo正在查询    loadSuccess查询成功     loadFaile查询失败，请检查网络连接*/
            val (loadGo, loadSuccess, loadFaile) = triple_dialog()
            loadGo.show()
            //设置当点击Dialog外时可以取消Dialog
            loadSuccess.setCanceledOnTouchOutside(true)
            loadFaile.setCanceledOnTouchOutside(true)
            loadGo.setCanceledOnTouchOutside(true)
            try {
                when (weekDay) {
                    1 -> weekString = "星期一"
                    2 -> weekString = "星期二"
                    3 -> weekString = "星期三"
                    4 -> weekString = "星期四"
                    5 -> weekString = "星期五"
                    6 -> weekString = "星期六"
                    0, 7 -> weekString = "星期日"
                }
                //判断时段
                var timeString = ""
                when (time) {
                    0 -> timeString = "全天"
                    1 -> timeString = "上午"
                    2 -> timeString = "下午"
                    3 -> timeString = "晚上"
                }
                /*以下这个点击事件，10.10优化了一下，如果responseData存在，则查询的数据来自本地（1s）
                * 如果responseData==null,则向服务器请求数据，时间天注定！*/
                if (responseData != null) {
                    date.text =
                        "你所查询的日期为：" + month + "月" + day + "日" + "(" + weekString + ")  " + timeString
                    //请求成功，开始渲染recyclerView界面
                    showSuccess(loadGo, loadSuccess)
                } else {
                    //responseData为空时
                    thread {
                        try {
                            var keyUrl: String = "" + xq + "/" + xq + month + day
                            var url: String =
                                "https://bistutu.github.io/emptyData/" + keyUrl + ".json"
                            responseData = okhttp_model.send(url).toString()
                            date.text =
                                "你所查询的日期为：" + month + "月" + day + "日" + "(" + weekString + ")  " + timeString
                            showSuccess(loadGo, loadSuccess)
                        } catch (e: Exception) {
                            //请求失败，发出警告
                            btSearch.postDelayed(
                                Runnable { loadGo.dismiss();loadFaile.show() },
                                1000
                            )
                            btSearch.postDelayed(Runnable { loadFaile.dismiss() }, 2000)
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
        //右上角的”查询声明“Button
        declareButton.setOnClickListener {
            when (xq) {
                1 -> {
                    MessageDialogBuilder(this)
                        .setTitle("小营校区查询的范围为：")
                        .setMessage(
                            "（1）一教阶梯教室\n" +
                                    "（2）二教全部\n" +
                                    "（3）四教全部\n\n" +
                                    "\t数据来自于教务网，不排除被临时占用的情况，仅供参考"
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
                                    "（3）三教阶梯教室\n\n" +
                                    "\t数据来自于教务网，不排除被临时占用的情况，仅供参考"
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
                                    "（3）三教全部\n\n" +
                                    "\t数据来自于教务网，不排除被临时占用的情况，仅供参考"
                        )
                        .addAction(
                            "已阅~"
                        ) { dialog, index -> dialog.dismiss() }
                        .show()
                }
            }
        }
    }

    private fun xyMethod(editor: SharedPreferences.Editor) {
        if (xy_select == 0)
            one.isChecked = true
        else
            noOne.isChecked = true
        //判断是否显示xy_selector
        if (xq == 1)
            xy_selector.visibility = View.VISIBLE
        else
            xy_selector.visibility = View.GONE
        one.setOnClickListener {
            xy_select = 0
            editor.putInt("xy_select", xy_select)
            editor.apply()
        }
        noOne.setOnClickListener {
            xy_select = 1
            editor.putInt("xy_select", xy_select)
            editor.apply()
        }
    }
    /*onCreate()方法结束*/

    //日期选择器，待更改——————>tomorrow如果跳转到了下个月或明年，则查询Faile——————————<
    private fun date_selector() {
        today.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
            //判断星期几
            weekDay = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1
            responseData = null
            btSearch.setBackgroundResource(R.drawable.ellipse_button_initial)
            btSearch.text = "正在加载中..."
            btSearch.isClickable = false
            btSearch.postDelayed({
                btSearch.setBackgroundResource(R.drawable.button_selected)
                btSearch.text = "查询空教室"
                btSearch.isClickable = true
            }, 1000)
            thread {
                try {
                    var keyUrl: String = "" + xq + "/" + xq + month + day
                    var url: String = "https://bistutu.github.io/emptyData/" + keyUrl + ".json"
                    responseData = okhttp_model.send(url).toString()
                } catch (e: Exception) {
                }
            }
        }
        tomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 1
            weekDay = gregorianCalendar.get(Calendar.DAY_OF_WEEK)
            responseData = null
            btSearch.setBackgroundResource(R.drawable.ellipse_button_initial)
            btSearch.text = "正在加载中..."
            btSearch.isClickable = false
            btSearch.postDelayed({
                btSearch.setBackgroundResource(R.drawable.button_selected)
                btSearch.text = "查询空教室"
                btSearch.isClickable = true
            }, 1000)
            thread {
                try {
                    var keyUrl: String = "" + xq + "/" + xq + month + day
                    var url: String = "https://bistutu.github.io/emptyData/" + keyUrl + ".json"
                    responseData = okhttp_model.send(url).toString()
                } catch (e: Exception) {
                }
            }
        }
        afterTomorrow.setOnClickListener {
            day = gregorianCalendar.get(Calendar.DAY_OF_MONTH) + 2
            weekDay = (gregorianCalendar.get(Calendar.DAY_OF_WEEK) + 1) % 7
            responseData = null
            btSearch.setBackgroundResource(R.drawable.ellipse_button_initial)
            btSearch.text = "正在加载中..."
            btSearch.isClickable = false
            btSearch.postDelayed({
                btSearch.setBackgroundResource(R.drawable.button_selected)
                btSearch.text = "查询空教室"
                btSearch.isClickable = true
            }, 1000)
            thread {
                try {
                    var keyUrl: String = "" + xq + "/" + xq + month + day
                    var url: String = "https://bistutu.github.io/emptyData/" + keyUrl + ".json"
                    responseData = okhttp_model.send(url).toString()
                } catch (e: Exception) {
                }
            }
        }
    }

    //校区选择Button，无需更改
    private fun xq_selector(
        items: Array<String>, editor: SharedPreferences.Editor
    ) {
        xq_selector.setOnClickListener {
            MenuDialogBuilder(this)
                .addItems(items) { dialog, which ->
                    Toast.makeText(this, "你选择了 " + items[which], Toast.LENGTH_SHORT).show()
                    xq_selector.text = items[which]
                    xq = which + 1
                    if (xq == 1)
                        xy_selector.visibility = View.VISIBLE
                    else
                        xy_selector.visibility = View.GONE
                    editor.putInt("campus", which)
                    editor.apply()
                    responseData = null
                    thread {
                        try {
                            var keyUrl: String = "" + xq + "/" + xq + month + day
                            var url: String =
                                "https://bistutu.github.io/emptyData/" + keyUrl + ".json"
                            responseData = okhttp_model.send(url).toString()
                        } catch (e: Exception) {
                        }
                    }

                    dialog.dismiss()
                }
                .show()
        }
    }


    //四个checkButton事件（时段选择器）
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

    //每次打开软件时的默认选择行为
    private fun defaultSelected() {
        today.isChecked = true
        allDay.isChecked = true
        morning.isChecked = true
        afternoon.isChecked = true
        night.isChecked = true
    }

    //网络请求成功时的操作 (渲染recyclerView)
    private fun showSuccess(loadGo: Dialog, loadSuccess: Dialog) {
        runOnUiThread {
            try {
                btSearch.postDelayed(Runnable { loadGo.dismiss();loadSuccess.show() }, 500)
                btSearch.postDelayed(Runnable { loadSuccess.dismiss(); }, 1000)
                val list = mutableListOf(empty_list("0", "", "", ""))
                val emptyList_all = gson.fromJson<List<empty_list>>(responseData, typeOf)
                for (i in emptyList_all) {
                    list.add(i)
                }
                val listArray = ArrayList<empty_list>(list)
                val timeList = ArrayList<empty_list>()
                var isTime = 0
                var isOneLine = 1
                var isXySelect=0
                for (i in listArray) {
                    if (i.em4.equals("" + time))
                        isTime = 1
                    if (i.em4.equals("" + (time + 1)))
                        isTime = 0
                    if (isTime != 0) {
                        if (isOneLine != 1) {
                            if (xq == 1) {
                                if(i.em3.equals(""+xy_select))
                                    isXySelect=1
                                if(i.em3.equals(""+(xy_select+1)))
                                    isXySelect=0
                                if (isXySelect==1){
                                    timeList.add(i)
                                }
                            } else
                                timeList.add(i)
                        }
                        isOneLine = 0
                    }
                }
                recyclerView.layoutManager = layoutManager
                val adapter = emptyListAdapter(this, timeList, xq)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                btSearch.postDelayed(Runnable {
                    loadSuccess.dismiss();
                    loadGo.dismiss();
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

    //menu事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.line1 -> {
                MessageDialogBuilder(this)
                    .setTitle("使用指南")
                    .setMessage(R.string.guide)
                    .addAction(
                        "已阅~"
                    ) { dialog, index -> dialog.dismiss() }
                    .show()
            }
            R.id.line2 -> {
                Toast.makeText(this, "正在检查更新...", Toast.LENGTH_SHORT).show()
                thread {
                    try {
                        val isUpdateJSON =
                            okhttp_model.send("https://bistutu.github.io/BistutuApk/update.json")
                        val updateData = gson.fromJson(isUpdateJSON, updateObject::class.java)
                        //获取用户现在的软件版本号
                        val manager = this.getPackageManager();
                        val versionNumber =
                            manager.getPackageInfo(this.getPackageName(), 0).versionName + ""
                        //是否需要更新？
                        if (!updateData.isUpdate.equals(versionNumber)) {
                            updateDialog(updateData, 1)
                        } else
                            updateDialog(updateData, 0)
                    } catch (e: Exception) {

                    }
                }
            }
            R.id.line3 -> {
                val intent = Intent(this, about::class.java)
                startActivity(intent)
            }

        }
        return true

    }

    private fun updateDialog(updateData: updateObject, judge: Int) {
        runOnUiThread {
            if (judge == 0) {
                Toast.makeText(this, "你的软件已是最新版本~", Toast.LENGTH_LONG).show()
                return@runOnUiThread
            }
            Toast.makeText(this, "新版本来了~", Toast.LENGTH_SHORT).show()
            MessageDialogBuilder(this)
                .setTitle("新版本来了~")
                .setMessage(updateData.updateMessage)
                .setCanceledOnTouchOutside(false)
                .addAction(
                    "立即更新"
                ) { dialog, index ->
                    Toast.makeText(this, "正在打开下载网址~", Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(updateData.updateUrl)
                    startActivity(intent)
                }
                .show()
        }
    }


    //三个Dialog，无需更改
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
                .setTipWord("查询失败，请检查网络设置后重试")
                .create(true)
        return Triple(loadGo, loadSuccess, loadFaile)
    }

    /*
    变量
    * */
    var time = 0    //时段，0为全天   1为上午    2为下午    3为晚上
    var xq = 1      //校区，1为小营   2为健翔桥   3为清河    （取自教务网数据）
    val items = arrayOf("小营校区", "健翔桥校区", "清河校区")    //校区选择按钮的文本

    //获取日期的GregorianCalendar
    val gregorianCalendar = GregorianCalendar()
    var month: Int = gregorianCalendar.get(Calendar.MONTH) + 1
    var day: Int = gregorianCalendar.get(Calendar.DAY_OF_MONTH)

    //判断所查询的日期为星期几
    var weekString = ""
    var weekDay: Int = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1

    //创建Gson对象
    val gson = Gson()
    val typeOf = object : TypeToken<List<empty_list>>() {}.type
    val layoutManager = LinearLayoutManager(this)

    var responseData: String? = null

    //定义一个xy_selector的辅助变量
    var xy_select = 0
}
/*  结束  */
