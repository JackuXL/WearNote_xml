package cn.wearbbs.note.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.wearbbs.note.R
import cn.wearbbs.note.database.bean.Note
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NoteAdapter(private var dataSet:List<Note>, private val listener: OnItemClickListener) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun onItemLongClick(position: Int)
    }

    fun setDataSet(dataList: List<Note>) {
        this.dataSet = dataList
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val date: TextView = view.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataSet[position].name
        holder.date.text = formatMemoDate(Date(Timestamp(dataSet[position].createTime).time))
        holder.itemView.setOnClickListener {
            // start activity

            listener.onItemClick(holder.adapterPosition)
        }

        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(holder.adapterPosition)
            return@setOnLongClickListener true
        }
    }

    private fun formatMemoDate(date: Date): String {
        // Get the current date and time
        val now = Calendar.getInstance()

        val memoDate = Calendar.getInstance().apply {
            time = date
        }
        // Calculate the date difference
        val diff = now.get(Calendar.DAY_OF_YEAR) - memoDate.get(Calendar.DAY_OF_YEAR)
        val days = diff.toLong()

        return when {
            days == 0L -> {
                // If it's today, display the specific time
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(memoDate.time)
            }

            days == 1L -> "昨天 "
            days < 7L -> {
                // If it's within a week, display the day of the week
                SimpleDateFormat("EEEE", Locale.getDefault()).format(memoDate.time)
            }

            else -> {
                // Otherwise, display the full date
                SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(memoDate.time)
            }
        }
    }
}
