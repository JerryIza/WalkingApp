package com.example.trailit.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.trailit.repositories.MainRepository
import javax.inject.Inject

//@ViewModelInject we get around creating a factory
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository

): ViewModel() {
}