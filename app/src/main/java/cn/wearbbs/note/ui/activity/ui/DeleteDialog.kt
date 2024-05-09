package cn.wearbbs.note.ui.activity.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import cn.wearbbs.note.R

class DeleteDialog(context: Context, themeResId: Int) : Dialog(context, themeResId) {

   var onItemClickListener : OnItemClickListener ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_delete)
        initView()
    }


    fun initView() {
        val llCancel: LinearLayout = findViewById(R.id.ll_cancel)
        val llDelete: LinearLayout = findViewById(R.id.ll_delete)
        llCancel.setOnClickListener {
            onItemClickListener?.onCancelClick()
        }

        llDelete.setOnClickListener {
            onItemClickListener?.onDeleteClick()

        }
    }

    override fun show() {
        super.show()
        val window = this.window
        if (window == null) {
        } else {
            val params = window.attributes
            params.width = -1
            params.height = -1
            window.decorView.setPadding(0, 0, 0, 0)
            window.attributes = params
        }
    }
    interface OnItemClickListener {
        fun onCancelClick()

        fun onDeleteClick()
    }


}