package com.example.trailit.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailit.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

//@ViewModelInject we get around creating a factory
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository

): ViewModel() {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()
    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    suspend fun getDbSize(): Int {
        return  mainRepository.getDbCount()
    }
}