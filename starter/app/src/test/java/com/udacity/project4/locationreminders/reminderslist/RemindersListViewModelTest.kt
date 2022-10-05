package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.app.Instrumentation
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.ExpectFailure
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

private lateinit var dataSource: FakeDataSource
private lateinit var viewModel: RemindersListViewModel
private lateinit var  remindersList: MutableList<ReminderDTO>

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp()= runBlocking{
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),dataSource)

         remindersList = mutableListOf(
            ReminderDTO("title","desc.","loc.",1.0,1.0),
            ReminderDTO("title","desc.","loc.",1.0,1.0),
            ReminderDTO("title","desc.","loc.",1.0,1.0)
        )
        dataSource.saveReminder(remindersList[0])
        dataSource.saveReminder(remindersList[1])
        dataSource.saveReminder(remindersList[2])
    }

    @After
    fun tearDown()= runBlocking {
        stopKoin()
        dataSource.deleteAllReminders()
    }

    @Test
    fun loadReminders_loading(){
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.value,`is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.value,`is`(false))
    }

    @Test
    fun getRemindersList(){
        viewModel.loadReminders()
        assertThat( viewModel.remindersList.value?.size, `is`(remindersList.size))
    }

    @Test
    fun loadReminders_ReturnError(){
        dataSource.setReturnError(true)
        viewModel.loadReminders()
        assertThat(viewModel.showSnackBar.value,`is`("Reminders were unable to get retrieved"))
    }

}