package com.thinkstu.myapplication
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ListAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*

import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MenuDialogBuilder
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import kotlinx.android.synthetic.main.recycler_view_model.*
import kotlinx.android.synthetic.main.recycler_view_model.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import kotlin.concurrent.thread
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tb1)
        val items = arrayOf("小营校区", "健翔桥校区", "清河校区")
        //初始化SharedPreferences
        val editor = getPreferences(MODE_PRIVATE).edit()
        val prefs = getPreferences(MODE_PRIVATE)

        //初始化校区选择器
        xq_selector.text = items[prefs.getInt("campus", 0)]
        //默认选择allDay与today
        today.isChecked = true
        allDay.isChecked = true
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
                    editor.putInt("campus", which)
                    editor.apply()
                    dialog.dismiss()
                }
                .show()
        }

        //查询按钮,1.5s
        btSearch.setOnClickListener {
            //Start
            val loadGo = QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("正在查询")
                    .create(true)
            val loadSuccess = QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                    .setTipWord("查询成功")
                    .create(true)
            val loadFaile = QMUITipDialog.Builder(this).setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                    .setTipWord("查询失败，请检查网络设置")
                    .create(true)
            loadGo.show()

            //先发送网络请求、线程
            thread {
                try {
                    val okhttp = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://bistutu.github.io/demoEmpty/0209191.txt")
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
                        showSuccess(responseData)
                    }

                } catch (e: Exception) {
                    //判断网络请求：Faile
                    btSearch.postDelayed(Runnable {
                        //跳出请求成功的Tip
                        loadGo.dismiss()
                        loadFaile.show()
                        showFaile()
                    }, 500)
                    e.printStackTrace()
                }
            }

            //延迟处理事件
            btSearch.postDelayed(Runnable {
                loadSuccess.dismiss()
                loadFaile.dismiss()
            }, 2000)
//
        }

    }

    //网络请求失败时的操作
    private fun showFaile() {
        runOnUiThread {
        }
    }

    //网络请求成功时的操作
    private fun showSuccess(responseData: String) {
        runOnUiThread {
            class emptyListAdapter(val context: Context, val emptyList: List<empty_list>) :
                RecyclerView.Adapter<emptyListAdapter.ViewHolder>() {
                inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
                    val view=LayoutInflater.from(context).inflate(R.layout.recycler_view_model,parent,false)
                    return ViewHolder(view)
                }
                override fun onBindViewHolder(holder: emptyListAdapter.ViewHolder, position: Int) {
                    val empty=emptyList[position]
                    holder.cardtv1.text=empty.em1
                    holder.cardtv2.text=empty.em2
                    holder.cardtv3.text=empty.em3
                    holder.cardtv4.text=empty.em4
                    if(holder.cardtv3.text=="")
                    {
                        holder.mc1.setBackgroundResource(R.color.split)
                        holder.mc2.visibility=View.GONE
                        holder.mc3.visibility=View.GONE
                        holder.mc4.visibility=View.GONE
                    }
                }
                override fun getItemCount(): Int =emptyList.size
            }
            var list=mutableListOf(empty_list("阶梯教室","","",""))
/*            val list= mutableListOf(empty_list("1","2","3","4"),
                empty_list("1","2","3","4"),
                empty_list("1","2","3","4"))*/
            //重新书写list——————————————————————————————————————————————————————————
            val gson=Gson()
            val typeOf=object : TypeToken<List<empty_list>>(){}.type
            val emptyList_all= gson.fromJson<List<empty_list>>(responseData,typeOf)
            for (i in emptyList_all) {
                list.add(i)
            }

            val listArray=ArrayList<empty_list>(list)
            val layoutManager=LinearLayoutManager(this)
            recyclerView.layoutManager=layoutManager
            val adapter=emptyListAdapter(this,listArray)
            recyclerView.adapter=adapter
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