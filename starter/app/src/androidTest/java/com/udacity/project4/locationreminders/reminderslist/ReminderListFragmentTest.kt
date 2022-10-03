package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.

    @Before
    fun setUp(){
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        stopKoin()

        val myModule = module {
                single {
                    reminderListViewModel
                }
        }

        startKoin {
            modules(listOf(myModule))
        }
    }

    @After
    fun tearDown()= runBlocking{
        fakeDataSource.deleteAllReminders()
    }


    @Test
    fun reminder_CheckItIsDisplayedInUI()= runBlockingTest{
        fakeDataSource.saveReminder( ReminderDTO("title","desc","loc",1.0,1.0))
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        onView(withId(R.id.reminderCardView)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.title)).check(ViewAssertions.matches(withText("title")))
        onView(withId(R.id.description)).check(ViewAssertions.matches(withText("desc")))
        onView(withId(R.id.location)).check(ViewAssertions.matches(withText("loc")))
    }
//    TODO: add testing for the error messages.
    @Test
    fun noReminders_ShouldShowError(){
    val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
    onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun addNewReminder_NavigateToSelectLocationFragment(){
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}