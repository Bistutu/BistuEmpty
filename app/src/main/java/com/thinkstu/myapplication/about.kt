package com.thinkstu.myapplication

import android.app.Notification
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MessageDialogBuilder

import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction




class about : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        //获取软件版本号
        val manager = this.getPackageManager();
        val info = manager.getPackageInfo(this.getPackageName(), 0);
        var version = "软件版本V "+info.versionName;
        versionTetx.text=version    //软件版本号

        back.setOnClickListener {
            this.finish()
        }
        github.setOnClickListener {
            val intent=Intent(Intent.ACTION_VIEW)
            intent.data=Uri.parse("https://github.com/Bistutu/emptyClassroom")
            startActivity(intent)
            Toast.makeText(this,"正在打开github",Toast.LENGTH_LONG).show()
        }
        /*wx.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text="Bistutu"
            Toast.makeText(this,"已复制“Bistutu”",Toast.LENGTH_LONG).show()
        }*/
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
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text="githubson"
            Toast.makeText(this,"已复制作者微信号~",Toast.LENGTH_LONG).show()
        }

    }
}