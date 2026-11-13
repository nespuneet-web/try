package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "additions")
data class AdditionEntry(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val firstNumber: Double,
  val secondNumber: Double,
  val total: Double,
  val note: String = "",
  val createdAtMillis: Long = System.currentTimeMillis(),
)
