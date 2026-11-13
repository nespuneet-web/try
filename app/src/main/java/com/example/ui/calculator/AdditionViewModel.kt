package com.example.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdditionEntry
import com.example.data.repository.AdditionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HistoryUiState {
  data object Loading : HistoryUiState
  data class Success(val entries: List<AdditionEntry>) : HistoryUiState
  data class Error(val message: String) : HistoryUiState
}

data class CalculatorInputState(
  val firstInput: String = "",
  val secondInput: String = "",
  val noteInput: String = "",
  val firstError: Boolean = false,
  val secondError: Boolean = false,
  val lastCalculatedFirst: Double? = null,
  val lastCalculatedSecond: Double? = null,
  val lastCalculatedTotal: Double? = null,
  val isSaving: Boolean = false,
  val editingEntry: AdditionEntry? = null,
  val showClearAllConfirm: Boolean = false,
) {
  val livePreviewTotal: Double?
    get() {
      val a = NumberFormatter.parseDoubleStrict(firstInput) ?: return null
      val b = NumberFormatter.parseDoubleStrict(secondInput) ?: return null
      val sum = a + b
      return if (sum.isFinite()) sum else null
    }
}

sealed interface AdditionEvent {
  data class ShowMessage(val message: String) : AdditionEvent
}

class AdditionViewModel(
  private val repository: AdditionRepository,
) : ViewModel() {

  private val _inputState = MutableStateFlow(CalculatorInputState())
  val inputState: StateFlow<CalculatorInputState> = _inputState.asStateFlow()

  private val _events = MutableSharedFlow<AdditionEvent>()
  val events: SharedFlow<AdditionEvent> = _events.asSharedFlow()

  val historyState: StateFlow<HistoryUiState> =
    repository
      .observeAdditions()
      .map<List<AdditionEntry>, HistoryUiState> { HistoryUiState.Success(it) }
      .catch { error ->
        emit(HistoryUiState.Error(error.localizedMessage ?: "Failed to load saved calculations"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HistoryUiState.Loading,
      )

  fun onFirstInputChanged(value: String) {
    _inputState.update { it.copy(firstInput = value, firstError = false) }
  }

  fun onSecondInputChanged(value: String) {
    _inputState.update { it.copy(secondInput = value, secondError = false) }
  }

  fun onNoteChanged(value: String) {
    val clipped = value.take(AdditionRepository.MAX_NOTE_LENGTH)
    _inputState.update { it.copy(noteInput = clipped) }
  }

  fun swapNumbers() {
    _inputState.update {
      it.copy(
        firstInput = it.secondInput,
        secondInput = it.firstInput,
        firstError = false,
        secondError = false,
      )
    }
  }

  fun clearInputs() {
    _inputState.update {
      it.copy(
        firstInput = "",
        secondInput = "",
        noteInput = "",
        firstError = false,
        secondError = false,
      )
    }
  }

  fun loadEntryIntoCalculator(entry: AdditionEntry) {
    _inputState.update {
      it.copy(
        firstInput = NumberFormatter.formatNumber(entry.firstNumber),
        secondInput = NumberFormatter.formatNumber(entry.secondNumber),
        noteInput = entry.note,
        firstError = false,
        secondError = false,
        lastCalculatedFirst = entry.firstNumber,
        lastCalculatedSecond = entry.secondNumber,
        lastCalculatedTotal = entry.total,
      )
    }
  }

  fun addAndSave() {
    val current = _inputState.value
    if (current.isSaving) return

    val first = NumberFormatter.parseDoubleStrict(current.firstInput)
    val second = NumberFormatter.parseDoubleStrict(current.secondInput)

    if (first == null || second == null) {
      _inputState.update {
        it.copy(firstError = first == null, secondError = second == null)
      }
      return
    }

    val total = first + second
    if (!total.isFinite()) {
      viewModelScope.launch {
        _events.emit(AdditionEvent.ShowMessage("Sum exceeds finite numeric range"))
      }
      return
    }

    val note = current.noteInput.trim().take(AdditionRepository.MAX_NOTE_LENGTH)

    _inputState.update {
      it.copy(
        isSaving = true,
        firstError = false,
        secondError = false,
        lastCalculatedFirst = first,
        lastCalculatedSecond = second,
        lastCalculatedTotal = total,
      )
    }

    viewModelScope.launch {
      try {
        repository.addAddition(first, second, note)
        _inputState.update { it.copy(isSaving = false) }
        _events.emit(
          AdditionEvent.ShowMessage(
            "Saved ${NumberFormatter.formatDisplay(first)} + ${NumberFormatter.formatDisplay(second)} = ${NumberFormatter.formatDisplay(total)}"
          )
        )
      } catch (e: Exception) {
        _inputState.update { it.copy(isSaving = false) }
        _events.emit(
          AdditionEvent.ShowMessage(e.localizedMessage ?: "Failed to save calculation")
        )
      }
    }
  }

  fun startEditingNote(entry: AdditionEntry) {
    _inputState.update { it.copy(editingEntry = entry) }
  }

  fun dismissEditNote() {
    _inputState.update { it.copy(editingEntry = null) }
  }

  fun saveEditedNote(entry: AdditionEntry, newNote: String) {
    _inputState.update { it.copy(editingEntry = null) }
    viewModelScope.launch {
      repository.updateNote(entry, newNote)
      _events.emit(AdditionEvent.ShowMessage("Note updated"))
    }
  }

  fun deleteEntry(entryId: Long) {
    viewModelScope.launch {
      repository.deleteAddition(entryId)
      _events.emit(AdditionEvent.ShowMessage("Calculation removed"))
    }
  }

  fun requestClearAll() {
    _inputState.update { it.copy(showClearAllConfirm = true) }
  }

  fun dismissClearAll() {
    _inputState.update { it.copy(showClearAllConfirm = false) }
  }

  fun confirmClearAll() {
    _inputState.update { it.copy(showClearAllConfirm = false) }
    viewModelScope.launch {
      repository.clearAll()
      _events.emit(AdditionEvent.ShowMessage("All saved calculations cleared"))
    }
  }

  private companion object {
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
