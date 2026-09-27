package com.ai.jobfinder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.jobfinder.data.JobEntity
import com.ai.jobfinder.data.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class JobViewModel(repository: JobRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val allJobs = repository.observeJobs()

    val searchQuery: StateFlow<String> = query
    val jobs: StateFlow<List<JobEntity>> = combine(allJobs, query) { jobs, text ->
        val normalized = text.trim().lowercase()
        if (normalized.isBlank()) jobs else jobs.filter {
            listOf(it.title, it.company, it.location, it.description)
                .any { value -> value.lowercase().contains(normalized) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(value: String) {
        query.value = value
    }
}

