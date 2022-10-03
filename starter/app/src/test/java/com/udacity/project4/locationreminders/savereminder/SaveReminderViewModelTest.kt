package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var dataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var  remindersList: MutableList<ReminderDTO>

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp()= runBlocking{
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),dataSource)
//
//        remindersList = mutableListOf(
//            ReminderDTO("title","desc.","loc.",1.0,1.0),
//            ReminderDTO("title","desc.","loc.",1.0,1.0),
//            ReminderDTO("title","desc.","loc.",1.0,1.0)
//        )
//        dataSource.saveReminder(remindersList[0])
//        dataSource.saveReminder(remindersList[1])
//        dataSource.saveReminder(remindersList[2])
    }

    @After
    fun tearDown()= runBlocking {
        stopKoin()
        dataSource.deleteAllReminders()
    }

    @Test
    fun saveReminder_loading(){
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(ReminderDataItem("title","desc.","loc.",1.0,1.0))
        Assert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredData_ReturnError_EmptyTitle(){
        viewModel.validateEnteredData(ReminderDataItem(null,"desc.","loc.",1.0,1.0))
        Assert.assertThat(viewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_ReturnError_EmptyLocation(){
        viewModel.validateEnteredData(ReminderDataItem("title","desc.",null,1.0,1.0))
        Assert.assertThat(viewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }

}