package cn.wearbbs.note.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.wearbbs.note.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import cn.wearbbs.note.application.MainApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FullScreenActivity : AppCompatActivity() {
    private lateinit var textViewTime: TextView
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val id = intent.getIntExtra("id", -1)
        if(id==-1) return
        val tvContent:TextView = findViewById(R.id.tv_content)
        CoroutineScope(Dispatchers.Main).launch {
            val note = MainApplication.noteDao.findById(id)
            runOnUiThread{
                tvContent.text = note.content.ifBlank { "还没有输入内容哦" }
            }
        }
        tvContent.setOnLongClickListener {
            startActivity(Intent(this, EditActivity::class.java).putExtra("id", id))
            finish()
            true
        }
        tvContent.movementMethod = ScrollingMovementMethod();
        textViewTime = findViewById(R.id.textView_time)

        startUpdatingTime()
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

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}