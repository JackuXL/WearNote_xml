package cn.wearbbs.note.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
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
import cn.wearbbs.note.database.bean.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditActivity : AppCompatActivity() {
    private lateinit var textViewTime: TextView
    private var job: Job? = null
    private lateinit var note: Note
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val id = intent.getIntExtra("id", -1)
        if(id==-1) return
        CoroutineScope(Dispatchers.Main).launch {
            note = MainApplication.noteDao.findById(id)
            runOnUiThread{
                val tvTitle:TextView = findViewById(R.id.tv_title)
                val invisibleRename: EditText = findViewById(R.id.invisible_rename)

                tvTitle.text = note.name
                tvTitle.setOnClickListener {

                    invisibleRename.setOnKeyListener { _, keyCode, _ ->
                        if(keyCode==KeyEvent.KEYCODE_ENTER){
                            val inputText = invisibleRename.text.toString()
                            CoroutineScope(Dispatchers.Main).launch {
                                MainApplication.noteDao.update(note.apply {
                                    name = inputText
                                })
                            }
                            tvTitle.text = inputText
                            invisibleRename.text.clear()
                            invisibleRename.clearFocus()
                            return@setOnKeyListener true
                        }
                        println("haha"+keyCode)
                        false
                    }
                    invisibleRename.setText(note.name)
                    invisibleRename.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(invisibleRename,0)
                }
                val etContent:EditText = findViewById(R.id.et_content)
                etContent.setText(note.content)
            }
            val fabEnter:FloatingActionButton = findViewById(R.id.fab_enter)
            fabEnter.setOnClickListener {
                val etContent:EditText = findViewById(R.id.et_content)
                val index = etContent.selectionStart
                val content = etContent.editableText
                if(index==-1){
                    content.append("\n")
                } else{
                    content.insert(index, "\n")
                }
            }
        }

        // Save, Actually
        val btnShare : FrameLayout = findViewById(R.id.btn_share)
        btnShare.setOnClickListener {
            val etContent:TextView = findViewById(R.id.et_content)
            note.content = etContent.text.toString()
            CoroutineScope(Dispatchers.Main).launch{
                MainApplication.noteDao.update(note)
            }
            finish()
        }

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