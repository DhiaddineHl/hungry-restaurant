package com.hungry.restaurant.pos.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hungry.restaurant.pos.HungryPosApp
import com.hungry.restaurant.pos.data.model.StatsPeriod
import com.hungry.restaurant.pos.data.model.StatsSummary
import com.hungry.restaurant.pos.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatsViewModel(private val repository: StatsRepository) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.TODAY)
    val period: StateFlow<StatsPeriod> = _period.asStateFlow()

    private val _stats = MutableStateFlow<StatsSummary?>(null)
    val stats: StateFlow<StatsSummary?> = _stats.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        load()
    }

    fun selectPeriod(period: StatsPeriod) {
        _period.value = period
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _loading.value = true
            _stats.value = repository.fetch(_period.value)
            _loading.value = false
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HungryPosApp
                StatsViewModel(app.container.statsRepository)
            }
        }
    }
}
