package cn.wearbbs.note.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.wearbbs.note.database.bean.Config

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config")
    @SuppressWarnings
    fun getAll(): MutableList<Config>

    @Query("SELECT * FROM config WHERE name LIKE :name")
    @SuppressWarnings
    fun findByName(name: String): Config?

    @Insert
    @SuppressWarnings
    fun insertAll(vararg configs: Config)

    @Update
    @SuppressWarnings
    fun update(config: Config)

    @Delete
    @SuppressWarnings
    fun delete(config: Config)
}