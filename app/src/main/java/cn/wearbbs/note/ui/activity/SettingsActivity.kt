package cn.wearbbs.note.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.wearbbs.note.R
import cn.wearbbs.note.application.MainApplication
import cn.wearbbs.note.database.bean.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SettingsActivity : AppCompatActivity() {
    private lateinit var textViewTime: TextView
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack:FrameLayout = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val lyRecover:LinearLayout = findViewById(R.id.ly_recover)
        lyRecover.setOnClickListener {
            Intent(this, RecoveryActivity::class.java).also {
                startActivity(it)
            }
        }

        val lyFeedback:LinearLayout = findViewById(R.id.ly_feedback)
        lyFeedback.setOnClickListener {
            Toast.makeText(this, "请发送邮件至 jackuxl2019@gmail.com 反馈问题", Toast.LENGTH_LONG).show()
        }

        val tvVersion:TextView = findViewById(R.id.tv_version)
        tvVersion.text = "v"+packageManager.getPackageInfo(packageName, 0).versionName + " By JackuXL"

        refreshData()
        val lyTimeFormat:LinearLayout = findViewById(R.id.ly_time_format)
        lyTimeFormat.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch {
                if(MainApplication.configDao.findByName("timeFormat") == null){
                    MainApplication.configDao.insertAll(Config("timeFormat", "12"))
                }
                else if (MainApplication.configDao.findByName("timeFormat")!!.config == "24") {
                    MainApplication.configDao.update(Config("timeFormat", "12"))
                } else {
                    MainApplication.configDao.update(Config("timeFormat", "24"))
                }
                MainApplication.configDao.findByName("timeFormat")?.config?.let { it1 -> MainApplication.timeFormat = it1 }
                refreshData()
                Toast.makeText(this@SettingsActivity, "即将重启应用", Toast.LENGTH_SHORT).show()
                // 100ms
                delay(1000)
                triggerRebirth(this@SettingsActivity)
            }
        }

        textViewTime = findViewById(R.id.textView_time)
        startUpdatingTime()
    }
    fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
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

    private fun refreshData(){
        val tvTimeFormat:TextView = findViewById(R.id.tv_time_format)
        CoroutineScope(Dispatchers.Main).launch {
            val cnt = MainApplication.configDao.findByName("timeFormat")?.config ?: "24"
            runOnUiThread{
                tvTimeFormat.text = cnt+"小时制"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}