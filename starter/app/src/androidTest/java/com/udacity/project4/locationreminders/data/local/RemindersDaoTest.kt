package com.udacity.project4.locationreminders.data.local

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
//    TODO: Add testing implementation to the RemindersDao.kt

    @Before
    fun setUp(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_GetById() = runBlockingTest {
        // GIVEN - insert a reminder
        val reminder1 = ReminderDTO("Title", "Description", "location", 1.0, 1.0)
        database.reminderDao().saveReminder(reminder1)

        // WHEN - Get the reminder by id from the database
        val loaded = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder1.id))
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.latitude, `is`(reminder1.latitude))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
    }



    @Test
    fun getReminderById_NotFound()=runBlockingTest{
        val loaded = database.reminderDao().getReminderById("1") // db is empty so anu id won't be found
        Assert.assertNull(loaded)
    }

}