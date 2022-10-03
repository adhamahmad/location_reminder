package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.lang.Exception
import com.udacity.project4.locationreminders.data.dto.Result as Result1

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()):ReminderDataSource{

//    TODO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result1<List<ReminderDTO>> {
        if(shouldReturnError){
            return Result1.Error("No reminders found")
        }
        reminders?.let {
            return Result1.Success(it)
        }
        return Result1.Error("No reminders found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result1<ReminderDTO> {
        return try {
            com.udacity.project4.locationreminders.data.dto.Result.Success(reminders?.find { id  == it.id }!!)
        } catch (ex: Exception) {
            com.udacity.project4.locationreminders.data.dto.Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()

    }



}