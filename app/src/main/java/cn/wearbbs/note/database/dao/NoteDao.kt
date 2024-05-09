package cn.wearbbs.note.database.dao

import androidx.room.*
import cn.wearbbs.note.database.bean.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    @SuppressWarnings
    fun getAll(): MutableList<Note>

    @Query("SELECT * FROM note WHERE id LIKE :id")
    @SuppressWarnings
    fun findById(id: Int): Note

    @Query("SELECT * FROM note WHERE name LIKE :name LIMIT 1")
    @SuppressWarnings
    fun findByName(name: String): Note

    @Insert
    @SuppressWarnings
    fun insertAll(vararg notes: Note)

    @Delete
    @SuppressWarnings
    fun delete(note: Note)

    @Update
    @SuppressWarnings
    fun update(note: Note)
}