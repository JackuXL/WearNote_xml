package cn.wearbbs.note.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Config(
    @PrimaryKey var name: String,
    @ColumnInfo var config: String,
)