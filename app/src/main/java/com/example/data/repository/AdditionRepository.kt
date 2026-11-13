package com.example.data.repository

import com.example.data.local.AdditionDao
import com.example.data.local.AdditionEntry
import kotlinx.coroutines.flow.Flow

class AdditionRepository(private val additionDao: AdditionDao) {

  fun observeAdditions(): Flow<List<AdditionEntry>> = additionDao.observeAdditions()

  suspend fun addAddition(
    firstNumber: Double,
    secondNumber: Double,
    note: String = "",
  ): Long {
    require(firstNumber.isFinite() && secondNumber.isFinite()) {
      "Operands must be finite numbers"
    }
    val total = firstNumber + secondNumber
    require(total.isFinite()) { "Sum exceeds finite numeric bounds" }
    val cleanNote = note.trim().take(MAX_NOTE_LENGTH)
    return additionDao.insertAddition(
      AdditionEntry(
        firstNumber = firstNumber,
        secondNumber = secondNumber,
        total = total,
        note = cleanNote,
        createdAtMillis = System.currentTimeMillis(),
      )
    )
  }

  suspend fun updateNote(entry: AdditionEntry, newNote: String) {
    val cleanNote = newNote.trim().take(MAX_NOTE_LENGTH)
    additionDao.updateAddition(entry.copy(note = cleanNote))
  }

  suspend fun deleteAddition(id: Long) {
    additionDao.deleteAdditionById(id)
  }

  suspend fun clearAll() {
    additionDao.deleteAll()
  }

  companion object {
    const val MAX_NOTE_LENGTH = 80
  }
}
