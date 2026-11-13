package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AddUpDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdditionRepositoryTest {

  private lateinit var database: AddUpDatabase
  private lateinit var repository: AdditionRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database =
      Room.inMemoryDatabaseBuilder(context, AddUpDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repository = AdditionRepository(database.additionDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun addUpdateDeleteAndClear_persistCorrectlyInRoom() = runBlocking {
    val id1 = repository.addAddition(12.5, 7.5, "Lunch")
    val id2 = repository.addAddition(100.0, 250.0, "Groceries")

    var items = repository.observeAdditions().first()
    assertEquals(2, items.size)
    val firstSaved = items.first { it.id == id1 }
    assertEquals(20.0, firstSaved.total, 0.0001)
    assertEquals("Lunch", firstSaved.note)

    repository.updateNote(firstSaved, "Dinner")
    items = repository.observeAdditions().first()
    assertEquals("Dinner", items.first { it.id == id1 }.note)

    repository.deleteAddition(id2)
    items = repository.observeAdditions().first()
    assertEquals(1, items.size)

    repository.clearAll()
    items = repository.observeAdditions().first()
    assertTrue(items.isEmpty())
  }
}
