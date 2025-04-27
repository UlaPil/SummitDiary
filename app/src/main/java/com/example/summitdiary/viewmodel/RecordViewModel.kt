package com.example.summitdiary.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_CURRENT_HIKE_ID = "currentHikeId"
        private const val KEY_START_TIMESTAMP = "startTimestamp"
        private const val KEY_STOP_TIMESTAMP = "stopTimestamp"
    }

    var currentHikeId: Long?
        get() = savedStateHandle.get<Long>(KEY_CURRENT_HIKE_ID)
        set(value) {
            savedStateHandle.set(KEY_CURRENT_HIKE_ID, value)
        }

    var startTimestamp: Long?
        get() = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)
        set(value) {
            savedStateHandle.set(KEY_START_TIMESTAMP, value)
        }

    var stopTimestamp: Long?
        get() = savedStateHandle.get<Long>(KEY_STOP_TIMESTAMP)
        set(value) {
            savedStateHandle.set(KEY_STOP_TIMESTAMP, value)
        }

    fun startHike(hikeId: Long) {
        currentHikeId = hikeId
        startTimestamp = System.currentTimeMillis()
        stopTimestamp = null
    }

    fun stopHike() {
        stopTimestamp = System.currentTimeMillis()
    }

    fun getFormattedStartDate(): String? {
        return startTimestamp?.let {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.format(Date(it))
        }
    }

    fun getFormattedStartTime(): String? {
        return startTimestamp?.let {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.format(Date(it))
        }
    }

    fun getFormattedDuration(): String? {
        val start = startTimestamp
        val stop = stopTimestamp
        return if (start != null && stop != null) {
            val durationMillis = stop - start
            val minutes = (durationMillis / 1000) / 60
            val seconds = (durationMillis / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            null
        }
    }
}
