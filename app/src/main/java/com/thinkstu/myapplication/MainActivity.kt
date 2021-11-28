package com.thinkstu.myapplication

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.android.synthetic.main.recycler_view_model.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.*

class MainActivity : AppCompatActivity() {
    // 所有的全局变量均放在了最末尾，所以这里是看不到这些变量的
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
        // 首先发起网络请求，判断软件是否需要更新
        thread {
            try {
                val isUpdateJSON = okhttp_model.send("https://bistutu.github.io/BistutuUpdate/update.json")
                val updateData = gson.fromJson(isUpdateJSON, updateObject::class.java)
                // 获取用户的现在软件版本号
                val manager = this.packageManager
                val versionNumber = manager.getPackageInfo(this.packageName, 0).versionName + ""
                // 与最新版本号对比，判断是否需要更新？
                if (!updateData.isUpdate.equals(versionNumber)) {
                    updateDialog(updateData, 1)
                }
            } catch (e: Exception) {
                // 不执行任何操作
            }
        }
        /* 创建三个提示的dialog，分别是-1.正在查询-2.查询成功-3.查询失败*/
        val (loadGo, loadSuccess, loadFaile) = triple_dialog()
        // 设置当点击Dialog外时可以取消Dialog
        loadSuccess.setCanceledOnTouchOutside(true)
        loadFaile.setCanceledOnTouchOutside(true)
        loadGo.setCanceledOnTouchOutside(true)
        // 设置recyclerView的缓存大小为200条记录
        recyclerView.setItemViewCacheSize(200)
        // SharedPreferences存储一些信息,这里用来设置每次打开软件时默认选择的校区
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs = getPreferences(MODE_PRIVATE)
        // 设置第一次打开软件时默认选择的校区是”小营校区“，xq是（校区）的缩写
        xq = prefs.getInt("campus", 1) + 1
        xq_selector.text = items[xq - 1] //xy_selector默认选择
        xy_select = prefs.getInt("xy_select", 0)
        // 默认行为，设置每次打开软件都会默认选择 “今天”  和  “全天”
        defaultSelected()
        // 时段选择
        time = fourCheckAction()
        // 校区选择
        xq_selector(items, editor)
        // 日期选择
        date_selector(data_day_1, data_day_2, data_day_3)
        // 查询按钮事件
        selector(editor, prefs, loadGo, loadSuccess, loadFaile)
    }

    private fun selector(
        editor: SharedPreferences.Editor,
        prefs: SharedPreferences,
        loadGo: QMUITipDialog,
        loadSuccess: QMUITipDialog,
        loadFaile: QMUITipDialog
    ) {
        btSearch.setOnClickListener {
            //如果用户没有选择一个时段time，这里就会发出一个警告
            if (time == -1) {
                Messages.emitLong(this, "请选择一个时段")
                return@setOnClickListener
            }
            // 启动-正在查询-的Dialog
            loadGo.show()
            try {
                when (weekDay) {
                    1 -> weekString = "星期一";2 -> weekString = "星期二";3 -> weekString = "星期三"
                    4 -> weekString = "星期四";5 -> weekString = "星期五";6 -> weekString = "星期六"
                    0, 7 -> weekString = "星期日"
                }
                // 判断时段
                var timeString = ""
                when (time) {
                    0 -> timeString = "全天";1 -> timeString = "上午"
                    2 -> timeString = "下午";3 -> timeString = "晚上"
                }
                // infoMessages为查询的提示字段
                var infoMessages = "你所查询的日期为：" + month + "月" + day + "日" + "(" + weekString + ")  " + timeString
                /* 如果responseData不为空,则证明本地存在数据，无需服务器请求数据*/
                if (responseData != null) {
                    date.text = infoMessages
                    play(loadGo, loadSuccess)
                } else {
                    // responseData为空时，需要向服务器请求数据
                    thread {
                        try {
                            // keyUrl是关键的URL片段
                            var keyUrl: String = "" + xq + "/" + xq + month + day
                            var url: String = "https://www.thinkstu.com/" + keyUrl + ".json"
                            responseData = okhttp_model.send(url).toString()
                            // 修复
                            when(day){
                                DAY->data_day_1=responseData
                                (DAY+1)->data_day_2=responseData
                                (DAY+2)->data_day_3=responseData
                            }
                            editor.putString(day.toString(), responseData)
                            date.text = infoMessages
                            play(loadGo, loadSuccess)
                        } catch (e: Exception) {
                            // 请求失败时，向用户发出错误提示
                            btSearch.postDelayed(
                                Runnable { loadGo.dismiss();loadFaile.show() }, 1000
                            )
                            btSearch.postDelayed(Runnable { loadFaile.dismiss() }, 4000)
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    /* 日期选择器，在这里的话，存在跨年查询失败的Bug，未修改
    *  另外，每当用户点击点击此Button时，会自动发起一个查询的网络请求，目的是为了优化用户体验、获得最快的响应速度
    * */
    private fun date_selector(data_day_1: String?, data_day_2: String?, data_day_3: String?) {
        today.setOnClickListener {
            day = DAY
            weekDay = WEEKDAT
            month = MONTH
            delayReaction()
            noReemit()
        }
        tomorrow.setOnClickListener {
            weekDay = WEEKDAT + 1
            day = DAY + 1
            month = MONTH
            when (month) {
                1, 3, 5, 7, 8, 10, 12 -> {
                    if (day == 32) {
                        month += 1;day = 1
                    }
                }
                2 -> {
                    if (gregorianCalendar.isLeapYear(gregorianCalendar.get(Calendar.YEAR))) {
                        if (day == 30) {
                            day = 1;month += 1
                        }
                    } else {
                        if (day == 29) {
                            day = 1;month += 1
                        }
                    }
                }
                4, 6, 9, 11 -> {
                    if (day == 31) {
                        day = 1;month += 1
                    }
                }
            }
            delayReaction()
            noReemit()
        }
        afterTomorrow.setOnClickListener {
            weekDay = (WEEKDAT + 2) % 7
            month = MONTH
            day = DAY + 2
            when (month) {
                1, 3, 5, 7, 8, 10, 12 -> {
                    if (day == 32) {
                        month += 1;day = 1
                    }
                    if (day == 33) {
                        month += 1;day = 2
                    }
                }
                2 -> {
                    if (gregorianCalendar.isLeapYear(gregorianCalendar.get(Calendar.YEAR))) {
                        if (day == 30) {
                            day = 1;month += 1
                        }
                        if (day == 31) {
                            day = 2;month += 1
                        }
                    } else {
                        if (day == 29) {
                            day = 1;month += 1
                        }
                        if (day == 30) {
                            day = 2;month += 1
                        }
                    }
                }
                4, 6, 9, 11 -> {
                    if (day == 31) {
                        day = 1;month += 1
                    }
                    if (day == 32) {
                        day = 2;month += 1
                    }
                }
            }
            delayReaction()
            noReemit()
        }
    }

    private fun noReemit() {
        when (day) {
            DAY -> if (data_day_1 == null) {
                reemit(1)
            } else {
                responseData = data_day_1
            }
            (DAY + 1) -> if (data_day_2 == null) {
                reemit(2)
            } else {
                responseData = data_day_2
            }
            (DAY + 2) -> if (data_day_3 == null) {
                reemit(3)
            } else {
                responseData = data_day_3
            }
        }

    }

    // delayReaction是查询按钮的延迟事件，主要是对UI的优化
    private fun delayReaction() {
        responseData = null
        btSearch.setBackgroundResource(R.drawable.ellipse_button_initial)
        btSearch.isClickable = false
        btSearch.text = "正在加载中..."
        btSearch.postDelayed({
            btSearch.setBackgroundResource(R.drawable.button_selected)
            btSearch.isClickable = true
            btSearch.text = "查询空教室"
        }, 500)
    }

    // 重新发送网络请求
    private fun reemit(i: Int) {
        thread {
            try {
                var keyUrl: String = "" + xq + "/" + xq + month + day
                var url: String = "https://www.thinkstu.com/" + keyUrl + ".json"
                responseData = okhttp_model.send(url).toString()
                when(i){
                    1->data_day_1=responseData
                    2->data_day_2=responseData
                    3->data_day_3=responseData
                }
            } catch (e: Exception) {// 失败不进行任何操作
            }
        }
    }

    //校区选择Button，无需更改
    private fun xq_selector(
        items: Array<String>, editor: SharedPreferences.Editor
    ) {
        xq_selector.setOnClickListener {
            data_day_1=null
            data_day_2=null
            data_day_3=null
            MenuDialogBuilder(this)
                .addItems(items) { dialog, which ->
                    Messages.emitShort(this, "你选择了 " + items[which])
                    xq_selector.text = items[which]
                    xq = which + 1
                    editor.putInt("campus", which)
                    editor.apply()
                    responseData = null
                    dialog.dismiss()
                }
                .show()
        }
    }


    // 时段选择器checkButton事件（逻辑凑合着看吧）
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

    // 网络请求成功 ，渲染recyclerView
    fun play(loadGo: Dialog, loadSuccess: Dialog) {
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
                for (i in listArray) {
                    if (i.d.equals("" + time))
                        isTime = 1
                    if (i.d.equals("" + (time + 1)))
                        isTime = 0
                    if (isTime != 0) {
                        if (isOneLine != 1) {
                            // 这里才是最终的add操作
                            timeList.add(i)
                        }
                        isOneLine = 0
                    }
                }
                recyclerView.layoutManager = layoutManager
                val adapter = emptyListAdapter(this, timeList, xq)
                recyclerView.adapter = adapter
                //一条小的分割线显现，分割画面
                splitLine.visibility = View.VISIBLE
            } catch (e: Exception) {
                // 这里是程序已经向服务器请求成功，但是却发生了"意外"错误的兼容操作，防止程序崩溃。
                btSearch.postDelayed(Runnable { loadSuccess.dismiss();loadGo.dismiss(); }, 500)
                Messages.emitLong(this, "服务器开小差了，请稍后再试~")
            }
        }
    }

    // 填充ActionBar的menu菜单栏
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // 菜单栏的menu事件
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
                Messages.emitShort(this, "正在查询中...")
                thread {
                    try {
                        val isUpdateJSON = okhttp_model.send("https://bistutu.github.io/BistutuUpdate/update.json")
                        val updateData = gson.fromJson(isUpdateJSON, updateObject::class.java)
                        //获取用户现在的软件版本号
                        val manager = this.packageManager
                        val versionNumber = manager.getPackageInfo(this.packageName, 0).versionName + ""
                        //是否需要更新？
                        if (!updateData.isUpdate.equals(versionNumber)) {
                            updateDialog(updateData, 1)
                        } else
                            updateDialog(updateData, 0)
                    } catch (e: Exception) {
                        // 这里是发送网络的子线程
                        runOnUiThread {
                            Messages.emitLong(this, "好像没有网络唉~")
                        }
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
                Messages.emitLong(this, "你的软件已是最新版本~")
                return@runOnUiThread
            }
            Messages.emitLong(this, "新版本来了~")
            MessageDialogBuilder(this)
                .setTitle("版本介绍")
                .setMessage(updateData.updateMessage)
                .setCanceledOnTouchOutside(false)
                .addAction(
                    "立即更新"
                ) { dialog, index ->
                    Messages.emitLong(this, "正在打开下载地址~")
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(updateData.updateUrl)
                    startActivity(intent)
                }.show()
        }
    }

    //每次打开软件时的默认行为
    private fun defaultSelected() {
        today.isChecked = true
        allDay.isChecked = true
        morning.isChecked = true
        afternoon.isChecked = true
        night.isChecked = true
    }

    //三个Dialog，无需更改
    private fun triple_dialog(): Triple<QMUITipDialog, QMUITipDialog, QMUITipDialog> {
        val loadGo =
            QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在查询...").create(true)
        val loadSuccess =
            QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("查询成功").create(true)
        val loadFaile =
            QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                .setTipWord("查询失败，好像没有网络唉~").create(true)
        return Triple(loadGo, loadSuccess, loadFaile)
    }

    // 全局变量的声明
    var time = 0    //时段，0为全天   1为上午    2为下午    3为晚上
    var xq = 1      //校区，1为小营   2为健翔桥   3为清河
    val items = arrayOf("小营校区", "健翔桥校区", "清河校区","沙河校区")    //校区选择按钮的字符串文本

    //获取日期的GregorianCalendar
    val gregorianCalendar = GregorianCalendar()
    var month: Int = gregorianCalendar.get(Calendar.MONTH) + 1
    var day: Int = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
    val MONTH = month
    val DAY = day

    //判断所查询的日期为星期几
    var weekString = ""
    var weekDay: Int = gregorianCalendar.get(Calendar.DAY_OF_WEEK) - 1
    val WEEKDAT = weekDay

    //Gson
    val gson = Gson()
    val typeOf = object : TypeToken<List<empty_list>>() {}.type
    val layoutManager = LinearLayoutManager(this)

    var responseData: String? = null

    // 三个临时变量存放获取到的数据，以空间来换取"时间+服务器负载减轻"
    var data_day_1: String? = null
    var data_day_2: String? = null
    var data_day_3: String? = null
    //定义一个xy_selector的辅助变量
    var xy_select = 0
}/*  程序结束  */