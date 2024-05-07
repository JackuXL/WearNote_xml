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
    suspend fun getAll(): MutableList<Config>

    @Query("SELECT * FROM config WHERE name LIKE :name")
    suspend fun findByName(name: String): Config?

    @Insert
    suspend fun insertAll(vararg configs: Config)

    @Update
    suspend fun update(config: Config)

    @Delete
    suspend fun delete(config: Config)
}