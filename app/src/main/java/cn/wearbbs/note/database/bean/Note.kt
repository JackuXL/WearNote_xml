package cn.wearbbs.note.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo var name: String,
    @ColumnInfo var content: String = "",
    @ColumnInfo var createTime: Long
)