package cn.wearbbs.note.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.wearbbs.note.adapter.NoteAdapter
import cn.wearbbs.note.R
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import cn.wearbbs.note.application.MainApplication
import cn.wearbbs.note.database.bean.Config
import cn.wearbbs.note.database.bean.Note
import cn.wearbbs.note.ui.activity.ui.DeleteDialog
import cn.wearbbs.note.util.DealFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var textViewTime: TextView
    private var job: Job? = null
    private var deleteDialog: DeleteDialog? = null
    private var adapter:NoteAdapter ?= null
    private var dataSet : MutableList<Note> =  mutableListOf()
    private var recyclerView: SwipeRecyclerView? = null
    private var lyEmpty:LinearLayout?= null


    val SEND_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val SP_FIRST_USE = "SP_first_use"
    private val SP_IS_FIRST = "sp_is_first"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewTime = findViewById(R.id.textView_time)
        //首次进入应用 先申请权限，用户同意了静默迁移数据，拒绝正常进行下面流程
        requestPermission()
        val btnSettings: FrameLayout = findViewById(R.id.btn_settings)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val btnCreate: FrameLayout = findViewById(R.id.btn_create)
        val invisibleEditText: EditText = findViewById(R.id.invisible_edittext)
        btnCreate.setOnClickListener {
            invisibleEditText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(invisibleEditText, 0)
        }

        invisibleEditText.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_ENTER){
                val inputText = invisibleEditText.text.toString()
                if (TextUtils.isEmpty(inputText)){
                    Toast.makeText(this, "文件名不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnKeyListener true
                }
                CoroutineScope(Dispatchers.Main).launch {
                    MainApplication.noteDao.insertAll(Note(name = inputText, content = "", createTime = System.currentTimeMillis()))
                    Intent(this@MainActivity, EditActivity::class.java).apply {
                        putExtra("id", MainApplication.noteDao.findByName(inputText).id)
                        startActivity(this)
                    }
                }
                invisibleEditText.text.clear()
                invisibleEditText.clearFocus()
                return@setOnKeyListener true
            }
            false
        }


    }

    private fun requestPermission() {
        // 获取 SharedPreferences 实例
        val sharedPref = getSharedPreferences(SP_FIRST_USE, Context.MODE_PRIVATE)
        val isFirst = sharedPref.getBoolean(SP_IS_FIRST,false)
        if (!isFirst){
            with(sharedPref.edit()){
                putBoolean(SP_IS_FIRST,true)
                apply()
            }
            requestPermissions(SEND_PERMISSIONS, 1)
        }else{
            initData()
        }
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
                    CoroutineScope(Dispatchers.Main).launch {
                        val list =
                            DealFile.convertTxtToNoteList(DealFile.filterTxtFiles(DealFile.appDataDirectoryPath))
                        list.forEach {
                            if (it.name.isBlank()) {
                                it.name = "未命名"
                            }
                        }
                        MainApplication.noteDao.insertAll(*list.toTypedArray())
                        DealFile.clearFolder(DealFile.appDataDirectoryPath)
                    }
                    initData()
                } else {
                    initData()
                }
            }
        }
    }

    private fun initData(){
        CoroutineScope(Dispatchers.Main).launch {
            MainApplication.timeFormat = MainApplication.configDao.findByName("timeFormat")?.config ?: "24"
            if(MainApplication.configDao.findByName("firstStart")==null){
                MainApplication.noteDao.insertAll(
                    Note(name = "欢迎使用 WearNote v3",
                        content = "界面焕新，稳定护航。继续陪伴记录你的生活点滴！",
                        createTime = System.currentTimeMillis())
                )
                MainApplication.configDao.insertAll(Config("firstStart", "false"))
                MainApplication.configDao.insertAll(Config("timeFormat", "24"))
                Intent(this@MainActivity, WelcomeActivity::class.java).also {
                    startActivity(it)
                }
            }
            startUpdatingTime()
        }

        refreshData()
    }

    private fun refreshData(){
        CoroutineScope(Dispatchers.Main).launch {
            dataSet = MainApplication.noteDao.getAll()
            lyEmpty = findViewById(R.id.ly_empty)
            recyclerView = findViewById(R.id.recycler_view)
            if(dataSet.isEmpty()){
                recyclerView?.visibility = View.GONE
                lyEmpty?.visibility = View.VISIBLE
                return@launch
            }
            dataSet.reverse()
            runOnUiThread{
                recyclerView?.visibility = View.VISIBLE
                lyEmpty?.visibility = View.GONE
                adapter = NoteAdapter(dataSet, object : NoteAdapter.OnItemClickListener {
                    override fun onItemClick(position:Int) {
                        val note = dataSet[position]
                        if(note.content.isBlank()){
                            val intent = Intent(this@MainActivity, EditActivity::class.java).putExtra("id", note.id)
                            startActivity(intent, null)
                        }
                        else{
                            val intent = Intent(this@MainActivity, FullScreenActivity::class.java).putExtra("id", note.id)
                            startActivity(intent, null)
                        }
                    }

                    override fun onItemLongClick(position: Int) {
                        showDeleteDialog(position)
                    }

                })
                recyclerView?.adapter = adapter
                recyclerView?.layoutManager = LinearLayoutManager(this@MainActivity)
                recyclerView?.setLongPressDragEnabled(false) // 拖拽排序，默认关闭。
                recyclerView?.setItemViewSwipeEnabled(false) // 侧滑删除，默认关闭。

                val mItemMoveListener: OnItemMoveListener =  object : OnItemMoveListener{
                    override fun onItemMove(
                        srcHolder: RecyclerView.ViewHolder,
                        targetHolder: RecyclerView.ViewHolder
                    ): Boolean {
                        // 此方法在Item拖拽交换位置时被调用。
                        // 第一个参数是要交换为之的Item，第二个是目标位置的Item。

                        // 交换数据，并更新adapter。
                        val fromPosition: Int = srcHolder.getAdapterPosition()
                        val toPosition: Int = targetHolder.getAdapterPosition()
                        CoroutineScope(Dispatchers.Main).launch {
                            MainApplication.noteDao.update(dataSet[fromPosition].apply {
                                createTime = dataSet[toPosition].createTime.also {
                                    dataSet[toPosition].createTime = createTime
                                }
                                name = dataSet[toPosition].name.also {
                                    dataSet[toPosition].name = name
                                }
                                content = dataSet[toPosition].content.also {
                                    dataSet[toPosition].content = content
                                }
                            })
                            MainApplication.noteDao.update(dataSet[toPosition])
                            adapter!!.setDataSet(dataSet)
                            adapter!!.notifyItemMoved(fromPosition, toPosition)
                        }

                        return true
                    }

                    override fun onItemDismiss(srcHolder: RecyclerView.ViewHolder) {
                        // 此方法在Item在侧滑删除时被调用。

                        // 从数据源移除该Item对应的数据，并刷新Adapter。
                        val position: Int = srcHolder.getAdapterPosition()
                        CoroutineScope(Dispatchers.Main).launch {
                            MainApplication.noteDao.delete(dataSet[position])
                            dataSet.removeAt(position)
                            adapter!!.setDataSet(dataSet)
                            adapter!!.notifyItemRemoved(position)
                            adapter!!.notifyItemRangeChanged(position, dataSet.size - position)
                            if(dataSet.isEmpty()){
                                recyclerView?.visibility = View.GONE
                                lyEmpty?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                recyclerView?.setOnItemMoveListener(mItemMoveListener) // 监听拖拽，更新UI。
            }
        }
    }

    fun showDeleteDialog(position: Int) {
        if (deleteDialog == null) {
            deleteDialog = DeleteDialog(this,R.style.dialog_default_style)
            deleteDialog?.onItemClickListener = object : DeleteDialog.OnItemClickListener {
                override fun onCancelClick() {
                    deleteDialog?.dismiss()
                }

                override fun onDeleteClick() {
                    deleteDialog?.dismiss()
                    CoroutineScope(Dispatchers.Main).launch {
                        MainApplication.noteDao.delete(dataSet[position])
                        dataSet.removeAt(position)
                        adapter!!.setDataSet(dataSet)
                        adapter!!.notifyItemRemoved(position)
                        adapter!!.notifyItemRangeChanged(position, dataSet.size - position)
                        if(dataSet.isEmpty()){
                            recyclerView?.visibility = View.GONE
                            lyEmpty?.visibility = View.VISIBLE
                        }
                    }
                }

            }
        }
        deleteDialog?.show()
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) {
            refreshData()
        }
    }


    /**
     * 隐藏软键盘(可用于Activity，Fragment)
     */
    fun hideSoftKeyboard(context: Context, viewList: List<View>?) {
        if (viewList == null) return
        val inputMethodManager =
            context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        for (v in viewList) {
            inputMethodManager.hideSoftInputFromWindow(
                v.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }


}