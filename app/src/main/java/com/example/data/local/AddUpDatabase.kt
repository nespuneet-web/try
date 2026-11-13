package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AdditionEntry::class], version = 1, exportSchema = false)
abstract class AddUpDatabase : RoomDatabase() {
  abstract fun additionDao(): AdditionDao

  companion object {
    @Volatile private var INSTANCE: AddUpDatabase? = null

    fun getInstance(context: Context): AddUpDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                AddUpDatabase::class.java,
                "addup_database",
              )
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
