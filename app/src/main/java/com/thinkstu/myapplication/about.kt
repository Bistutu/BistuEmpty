package com.thinkstu.myapplication

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MessageDialogBuilder

class about : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        //获取软件版本号
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, 0)
        var version = "软件版本V "+info.versionName
        versionTetx.text=version    //软件版本号

        back.setOnClickListener {
            this.finish()
        }
        github.setOnClickListener {
            MessageDialogBuilder(this)
                .setTitle("Github开源地址")
                .setMessage("即将跳转至浏览器打开网页，是否确定？")
                .addAction(
                    "取消"
                ) { dialog, index -> dialog.dismiss()
                }
                .addAction(
                    "确定"
                ) { dialog, index -> dialog.dismiss()
                    val intent=Intent(Intent.ACTION_VIEW)
                    intent.data=Uri.parse("https://github.com/Bistutu/BistuEmpty")
                    startActivity(intent)
                    Msg.short(this,"正在打开github")
                    }
                .show()

        }

        update.setOnClickListener {
            MessageDialogBuilder(this)
                .setTitle("更新日志")
                .setMessage(R.string.updateString)
                .addAction(
                    "已阅~"
                ) { dialog, index -> dialog.dismiss() }
                .show()
        }
        contact.setOnClickListener {
            contact.isClickable=false
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text="githubson"
            Toast.makeText(this,"已复制作者微信号~",Toast.LENGTH_LONG).show()
            contact.postDelayed({contact.isClickable=true},500)
        }

    }
}