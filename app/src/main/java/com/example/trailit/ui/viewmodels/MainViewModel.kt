package com.example.trailit.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailit.db.Run
import com.example.trailit.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

//@ViewModelInject we get around creating a factory
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository

): ViewModel() {

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}