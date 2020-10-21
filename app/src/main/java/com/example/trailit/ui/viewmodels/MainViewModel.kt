package com.example.trailit.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailit.data.entitites.Run
import com.example.trailit.other.SortBy
import com.example.trailit.repositories.MainRepository
import kotlinx.coroutines.launch

//@ViewModelInject we get around creating a factory
class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository

) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()


    val runs = MediatorLiveData<List<Run>>()

    var sortBy = SortBy.DATE

    init {
        runs.addSource(runsSortedByDate) { results ->
            if (sortBy == SortBy.DATE) {
                results?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { results ->
            if (sortBy == SortBy.DISTANCE) {
                results?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { results ->
            if (sortBy == SortBy.CALORIES_BURNED) {
                results?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByMillis) { results ->
            if (sortBy == SortBy.RUNNING_TIME) {
                results?.let { runs.value = it }
            }
        }

    }

    fun sortRuns(sortBy: SortBy)  = when(sortBy){
        SortBy.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortBy.RUNNING_TIME -> runsSortedByMillis.value?.let { runs.value = it }
        SortBy.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortBy.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortBy.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortBy = sortBy
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}