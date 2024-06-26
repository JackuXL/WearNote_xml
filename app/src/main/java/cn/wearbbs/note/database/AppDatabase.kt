package cn.wearbbs.note.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cn.wearbbs.note.database.bean.Config
import cn.wearbbs.note.database.bean.Note
import cn.wearbbs.note.database.dao.ConfigDao
import cn.wearbbs.note.database.dao.NoteDao

@Database(entities = [Note::class, Config::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun configDao(): ConfigDao
}