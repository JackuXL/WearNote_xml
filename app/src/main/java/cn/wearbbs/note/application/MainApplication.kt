package cn.wearbbs.note.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.room.Room
import cn.wearbbs.note.database.AppDatabase
import cn.wearbbs.note.database.dao.ConfigDao
import cn.wearbbs.note.database.dao.NoteDao

/**
 * @author JackuXL
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "note"
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
        configDao = db.configDao()
    }

    //TODO
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        var timeFormat:String = "24"
        lateinit var noteDao: NoteDao
        lateinit var configDao: ConfigDao
    }
}