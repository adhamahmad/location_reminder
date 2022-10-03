package com.udacity.project4.locationreminders.data.local

import android.security.identity.ResultData
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @Before
    fun setup(){
    database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        RemindersDatabase::class.java
    )   .allowMainThreadQueries()
        .build()
    localDataSource = RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)
    }

    @After
    fun clearUp() = database.close()

    @Test
    fun saveReminder_GetReminder() = runBlocking{
        val reminder1 = ReminderDTO("Title", "Description", "location", 1.0, 1.0)
        localDataSource.saveReminder(reminder1)
       val loaded = localDataSource.getReminder(reminder1.id)

        loaded as Result.Success

        assertThat(loaded.data.id, `is`(reminder1.id))
        assertThat(loaded.data.title, `is`(reminder1.title))
        assertThat(loaded.data.description, `is`(reminder1.description))
        assertThat(loaded.data.location, `is`(reminder1.location))
        assertThat(loaded.data.latitude, `is`(reminder1.latitude))
        assertThat(loaded.data.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun deleteAllReminders_dataReminderIsEmpty() = runBlocking {
        val result = localDataSource.getReminders() // empty db

        result as Result.Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun saveReminder_GetReminder_ShouldReturnError() = runBlocking{
        val reminder1 = ReminderDTO("Title", "Description", "location", 1.0, 1.0)
        localDataSource.saveReminder(reminder1)
        val loaded = localDataSource.getReminder(reminder1.id+1) // wrong id

        loaded as Result.Error

        assertThat(loaded.message, `is`("Reminder not found!"))
    }
}