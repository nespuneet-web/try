package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdditionDao {
  @Query("SELECT * FROM additions ORDER BY createdAtMillis DESC, id DESC")
  fun observeAdditions(): Flow<List<AdditionEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAddition(entry: AdditionEntry): Long

  @Update
  suspend fun updateAddition(entry: AdditionEntry)

  @Query("DELETE FROM additions WHERE id = :id")
  suspend fun deleteAdditionById(id: Long)

  @Query("DELETE FROM additions")
  suspend fun deleteAll()
}
