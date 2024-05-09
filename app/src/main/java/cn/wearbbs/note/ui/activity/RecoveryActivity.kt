package cn.wearbbs.note.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.wearbbs.note.R
import cn.wearbbs.note.application.MainApplication
import cn.wearbbs.note.database.bean.Note
import cn.wearbbs.note.util.DealFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecoveryActivity : AppCompatActivity() {
    private val externalStoragePath = Environment.getExternalStorageDirectory().path
    private lateinit var textViewTime: TextView
    private var job: Job? = null

    private var granted = false
    val SEND_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnBack:FrameLayout = findViewById(R.id.btn_back)
        val lyFinish:LinearLayout = findViewById(R.id.ly_finish)
        btnBack.setOnClickListener {
            finish()
        }
        lyFinish.setOnClickListener {
            finish()
        }
        val lyPermission = findViewById<LinearLayout>(R.id.ly_permission)
        lyPermission.setOnClickListener {
            requestStoragePermission()
        }
        val lyRecover = findViewById<LinearLayout>(R.id.ly_recover)
        lyRecover.setOnClickListener {
            if(!granted){
                Toast.makeText(this, "需要文件存储权限才能执行此操作", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                val list = DealFile.convertTxtToNoteList(DealFile.filterTxtFiles(DealFile.appDataDirectoryPath))
                list.forEach {
                    if (it.name.isBlank()) {
                        it.name = "未命名"
                    }
                }
                MainApplication.noteDao.insertAll(*list.toTypedArray())
                DealFile.clearFolder(DealFile.appDataDirectoryPath)
                Toast.makeText(
                    this@RecoveryActivity,
                    "恢复成功%s个文件".replace("%s", list.size.toString()),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        textViewTime = findViewById(R.id.textView_time)
        startUpdatingTime()
    }

    private fun requestStoragePermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionGranted = PackageManager.PERMISSION_GRANTED


        when {
            ContextCompat.checkSelfPermission(this, permission) == permissionGranted -> {
                // 权限已经被授予，执行相关操作
                Toast.makeText(this, "文件存储权限已授予", Toast.LENGTH_SHORT).show()
                val tvPermission: TextView = findViewById(R.id.tv_permission)
                tvPermission.text = "已授予"
                granted = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                // 当用户拒绝权限请求时，显示一个解释的对话框
                // 您可以在这里显示一个对话框，解释为什么需要文件存储权限，并向用户请求授予权限
                Toast.makeText(this, "需要文件存储权限才能执行此操作", Toast.LENGTH_SHORT).show()
                requestPermission()
//                requestPermissionLauncher.launch(permission)
            }

            else -> {
                // 直接请求权限
                requestPermission()
//                requestPermissionLauncher.launch(permission)
            }
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 文件存储权限已授予
                Toast.makeText(this, "文件存储权限已授予", Toast.LENGTH_SHORT).show()
                // 执行相关操作
                granted = true
                val tvPermission: TextView = findViewById(R.id.tv_permission)
                tvPermission.text = "已授予"
            } else {
                // 文件存储权限被拒绝
                Toast.makeText(this, "文件存储权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }

    fun requestPermission() {
        requestPermissions(SEND_PERMISSIONS, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 文件存储权限已授予
                    Toast.makeText(this, "文件存储权限已授予", Toast.LENGTH_SHORT).show()
                    // 执行相关操作
                    granted = true
                    val tvPermission: TextView = findViewById(R.id.tv_permission)
                    tvPermission.text = "已授予"
                } else {
                    // 文件存储权限被拒绝
                    Toast.makeText(this, "文件存储权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startUpdatingTime() {
        var format = "HH:mm"
        CoroutineScope(Dispatchers.Main).launch {
            if(MainApplication.timeFormat == "12"){
                format = "hh:mm"
            }
        }
        job = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val currentTime = SimpleDateFormat(format, Locale.getDefault()).format(Date())
                textViewTime.text = currentTime
                delay(1000) // Delay for 1 second
            }
        }
    }
}