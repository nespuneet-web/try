package com.example.ui.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.local.AddUpDatabase
import com.example.data.local.AdditionEntry
import com.example.data.repository.AdditionRepository
import com.example.ui.theme.JetBrainsMonoFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFeatureScreen() {
  val viewModel: AdditionViewModel =
    viewModel(
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val dao = AddUpDatabase.getInstance(app).additionDao()
            AdditionViewModel(AdditionRepository(dao))
          }
        }
    )

  val inputState by viewModel.inputState.collectAsStateWithLifecycle()
  val historyState by viewModel.historyState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val hasEntries = (historyState as? HistoryUiState.Success)?.entries?.isNotEmpty() == true

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is AdditionEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
      }
    }
  }

  inputState.editingEntry?.let { entry ->
    EditNoteDialog(
      entry = entry,
      onDismiss = viewModel::dismissEditNote,
      onSave = { updatedNote -> viewModel.saveEditedNote(entry, updatedNote) },
    )
  }

  if (inputState.showClearAllConfirm) {
    ClearAllDialog(
      onDismiss = viewModel::dismissClearAll,
      onConfirm = viewModel::confirmClearAll,
    )
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      CenterAlignedTopAppBar(
        colors =
          TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(30.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.Add,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.titleLarge,
            )
          }
        },
        actions = {
          if (hasEntries) {
            IconButton(
              onClick = viewModel::requestClearAll,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("clear_all_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = stringResource(R.string.clear_all_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
      )
    },
  ) { innerPadding ->
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(innerPadding)
    ) {
      val isExpanded = maxWidth >= 700.dp

      if (isExpanded) {
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
          Column(
            modifier =
              Modifier.weight(0.46f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            CalculatorCard(
              inputState = inputState,
              onFirstChanged = viewModel::onFirstInputChanged,
              onSecondChanged = viewModel::onSecondInputChanged,
              onNoteChanged = viewModel::onNoteChanged,
              onSwap = viewModel::swapNumbers,
              onClear = viewModel::clearInputs,
              onAddAndSave = viewModel::addAndSave,
            )
            ResultReadoutCard(inputState = inputState)
          }

          LazyColumn(
            modifier = Modifier.weight(0.54f).fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            historySectionItems(
              historyState = historyState,
              onLoadEntry = viewModel::loadEntryIntoCalculator,
              onEditNote = viewModel::startEditingNote,
              onDeleteEntry = viewModel::deleteEntry,
            )
          }
        }
      } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
          LazyColumn(
            modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            item(key = "calculator_card") {
              CalculatorCard(
                inputState = inputState,
                onFirstChanged = viewModel::onFirstInputChanged,
                onSecondChanged = viewModel::onSecondInputChanged,
                onNoteChanged = viewModel::onNoteChanged,
                onSwap = viewModel::swapNumbers,
                onClear = viewModel::clearInputs,
                onAddAndSave = viewModel::addAndSave,
              )
            }
            item(key = "result_card") { ResultReadoutCard(inputState = inputState) }
            historySectionItems(
              historyState = historyState,
              onLoadEntry = viewModel::loadEntryIntoCalculator,
              onEditNote = viewModel::startEditingNote,
              onDeleteEntry = viewModel::deleteEntry,
            )
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
          }
        }
      }
    }
  }
}

@Composable
private fun CalculatorCard(
  inputState: CalculatorInputState,
  onFirstChanged: (String) -> Unit,
  onSecondChanged: (String) -> Unit,
  onNoteChanged: (String) -> Unit,
  onSwap: () -> Unit,
  onClear: () -> Unit,
  onAddAndSave: () -> Unit,
) {
  val focusManager = LocalFocusManager.current

  Card(
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      OutlinedTextField(
        value = inputState.firstInput,
        onValueChange = onFirstChanged,
        label = { Text(stringResource(R.string.first_number_label)) },
        placeholder = { Text("0") },
        isError = inputState.firstError,
        supportingText =
          if (inputState.firstError) {
            { Text(stringResource(R.string.error_enter_valid_first)) }
          } else null,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
        keyboardOptions =
          KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        trailingIcon = {
          if (inputState.firstInput.isNotEmpty()) {
            IconButton(
              onClick = { onFirstChanged("") },
              modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
              Icon(
                imageVector = Icons.Rounded.Clear,
                contentDescription = stringResource(R.string.clear_field_desc),
              )
            }
          }
        },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("first_number_input"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp).size(38.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Rounded.Add,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp),
            )
          }
        }
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledTonalIconButton(
          onClick = onSwap,
          colors =
            IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("swap_numbers_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.SwapVert,
            contentDescription = stringResource(R.string.swap_numbers_desc),
          )
        }
      }

      OutlinedTextField(
        value = inputState.secondInput,
        onValueChange = onSecondChanged,
        label = { Text(stringResource(R.string.second_number_label)) },
        placeholder = { Text("0") },
        isError = inputState.secondError,
        supportingText =
          if (inputState.secondError) {
            { Text(stringResource(R.string.error_enter_valid_second)) }
          } else null,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
        keyboardOptions =
          KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        trailingIcon = {
          if (inputState.secondInput.isNotEmpty()) {
            IconButton(
              onClick = { onSecondChanged("") },
              modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
              Icon(
                imageVector = Icons.Rounded.Clear,
                contentDescription = stringResource(R.string.clear_field_desc),
              )
            }
          }
        },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("second_number_input"),
      )

      OutlinedTextField(
        value = inputState.noteInput,
        onValueChange = onNoteChanged,
        label = { Text(stringResource(R.string.note_label)) },
        placeholder = { Text(stringResource(R.string.note_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions =
          KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              onAddAndSave()
            }
          ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().testTag("note_input"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedButton(
          onClick = onClear,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.weight(0.35f).height(52.dp).testTag("clear_inputs_button"),
        ) {
          Text(
            text = stringResource(R.string.clear_inputs),
            style = MaterialTheme.typography.labelLarge,
          )
        }

        Button(
          onClick = {
            focusManager.clearFocus()
            onAddAndSave()
          },
          enabled = !inputState.isSaving,
          shape = MaterialTheme.shapes.medium,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.weight(0.65f).height(52.dp).testTag("add_and_save_button"),
        ) {
          if (inputState.isSaving) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp,
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.Functions,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = stringResource(R.string.add_and_save),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ResultReadoutCard(inputState: CalculatorInputState) {
  val savedTotal = inputState.lastCalculatedTotal
  val previewTotal = inputState.livePreviewTotal
  val displayedTotal = savedTotal ?: previewTotal
  val isSavedResult =
    savedTotal != null &&
      NumberFormatter.parseDoubleStrict(inputState.firstInput) == inputState.lastCalculatedFirst &&
      NumberFormatter.parseDoubleStrict(inputState.secondInput) == inputState.lastCalculatedSecond

  val containerBrush =
    Brush.linearGradient(
      colors =
        listOf(
          MaterialTheme.colorScheme.secondary,
          MaterialTheme.colorScheme.secondary.copy(alpha = 0.88f),
        )
    )

  Surface(
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.secondary,
    shadowElevation = 4.dp,
    modifier = Modifier.fillMaxWidth().testTag("result_readout_card"),
  ) {
    Column(
      modifier = Modifier.background(containerBrush).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            if (isSavedResult || previewTotal == null) {
              stringResource(R.string.result_label)
            } else {
              stringResource(R.string.live_preview_label)
            },
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.78f),
        )

        AnimatedVisibility(
          visible = isSavedResult,
          enter = fadeIn() + scaleIn(),
          exit = fadeOut(),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.tertiary,
            shape = MaterialTheme.shapes.small,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Save,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(14.dp),
              )
              Text(
                text = stringResource(R.string.saved_to_db_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiary,
              )
            }
          }
        }
      }

      AnimatedContent(
        targetState = displayedTotal,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "total_animation",
      ) { value ->
        Text(
          text = if (value != null) NumberFormatter.formatDisplay(value) else "—",
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.onSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      val equationText =
        if (
          isSavedResult &&
            inputState.lastCalculatedFirst != null &&
            inputState.lastCalculatedSecond != null
        ) {
          "${NumberFormatter.formatDisplay(inputState.lastCalculatedFirst)} + ${
            NumberFormatter.formatDisplay(inputState.lastCalculatedSecond)
          } = ${NumberFormatter.formatDisplay(savedTotal!!)}"
        } else if (previewTotal != null) {
          val a = NumberFormatter.parseDoubleStrict(inputState.firstInput) ?: 0.0
          val b = NumberFormatter.parseDoubleStrict(inputState.secondInput) ?: 0.0
          "${NumberFormatter.formatDisplay(a)} + ${NumberFormatter.formatDisplay(b)}"
        } else {
          "Enter two numbers to compute their sum"
        }

      Text(
        text = equationText,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
      )
    }
  }
}

private fun androidx.compose.foundation.lazy.LazyListScope.historySectionItems(
  historyState: HistoryUiState,
  onLoadEntry: (AdditionEntry) -> Unit,
  onEditNote: (AdditionEntry) -> Unit,
  onDeleteEntry: (Long) -> Unit,
) {
  item(key = "history_header") {
    val entries = (historyState as? HistoryUiState.Success)?.entries.orEmpty()
    val cumulativeSum = entries.sumOf { it.total }
    Row(
      modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.History,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp),
        )
        Text(
          text = stringResource(R.string.history_section_title),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }
      if (historyState is HistoryUiState.Success && entries.isNotEmpty()) {
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = MaterialTheme.shapes.small,
        ) {
          Text(
            text =
              "${stringResource(R.string.entries_count_format, entries.size)} · ${
                stringResource(R.string.cumulative_sum_format, NumberFormatter.formatDisplay(cumulativeSum))
              }",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }
    }
  }

  when (historyState) {
    HistoryUiState.Loading -> {
      item(key = "history_loading") {
        Box(
          modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }

    is HistoryUiState.Error -> {
      item(key = "history_error") {
        Surface(
          color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = historyState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    }

    is HistoryUiState.Success -> {
      if (historyState.entries.isEmpty()) {
        item(key = "history_empty") { EmptyHistoryCard() }
      } else {
        items(items = historyState.entries, key = { it.id }) { entry ->
          AdditionHistoryCard(
            entry = entry,
            onClick = { onLoadEntry(entry) },
            onEditNote = { onEditNote(entry) },
            onDelete = { onDeleteEntry(entry.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyHistoryCard() {
  Card(
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_addup_hero_1794543367388),
        contentDescription = stringResource(R.string.hero_illustration_desc),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(150.dp).clip(MaterialTheme.shapes.medium),
      )
      Text(
        text = stringResource(R.string.empty_history_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = stringResource(R.string.empty_history_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun AdditionHistoryCard(
  entry: AdditionEntry,
  onClick: () -> Unit,
  onEditNote: () -> Unit,
  onDelete: () -> Unit,
) {
  val formattedTime = remember(entry.createdAtMillis) { formatMillis(entry.createdAtMillis) }

  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .testTag("history_entry_${entry.id}"),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text =
              "${NumberFormatter.formatDisplay(entry.firstNumber)} + ${
                NumberFormatter.formatDisplay(entry.secondNumber)
              }",
            style =
              MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
              ),
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = "= ${NumberFormatter.formatDisplay(entry.total)}",
              style =
                MaterialTheme.typography.titleMedium.copy(
                  fontFamily = JetBrainsMonoFamily,
                  fontWeight = FontWeight.Bold,
                ),
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }

        if (entry.note.isNotBlank()) {
          Text(
            text = entry.note,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Text(
          text = "$formattedTime · ${stringResource(R.string.load_into_calculator_hint)}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onEditNote,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("edit_note_${entry.id}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.EditNote,
            contentDescription = stringResource(R.string.edit_note_desc),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDelete,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("delete_entry_${entry.id}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = stringResource(R.string.delete_entry_desc),
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Composable
private fun EditNoteDialog(
  entry: AdditionEntry,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
) {
  var noteText by remember(entry.id) { mutableStateOf(entry.note) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.edit_note_dialog_title)) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text =
            "${NumberFormatter.formatDisplay(entry.firstNumber)} + ${
              NumberFormatter.formatDisplay(entry.secondNumber)
            } = ${NumberFormatter.formatDisplay(entry.total)}",
          style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
        )
        OutlinedTextField(
          value = noteText,
          onValueChange = { noteText = it.take(AdditionRepository.MAX_NOTE_LENGTH) },
          label = { Text(stringResource(R.string.note_label)) },
          placeholder = { Text(stringResource(R.string.note_placeholder)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("edit_note_dialog_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(noteText) },
        modifier = Modifier.testTag("edit_note_save_button"),
      ) {
        Text(stringResource(R.string.save_action))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }
    },
  )
}

@Composable
private fun ClearAllDialog(
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.clear_all_dialog_title)) },
    text = { Text(stringResource(R.string.clear_all_dialog_message)) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ),
        modifier = Modifier.testTag("clear_all_confirm_button"),
      ) {
        Text(stringResource(R.string.clear_all_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }
    },
  )
}

private fun formatMillis(millis: Long): String {
  val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
  return formatter.format(Date(millis))
}
