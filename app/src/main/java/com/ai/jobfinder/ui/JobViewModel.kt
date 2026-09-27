package com.ai.jobfinder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.jobfinder.data.JobEntity
import com.ai.jobfinder.data.JobRepository
import com.ai.jobfinder.data.SavedSearchEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JobViewModel(private val repository: JobRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val allJobs = repository.observeJobs()
    val savedSearches: StateFlow<List<SavedSearchEntity>> = repository.observeSavedSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            runCatching {
                repository.syncFromJooble(
                    api = com.ai.jobfinder.data.JoobleClient.api,
                    apiKey = com.ai.jobfinder.BuildConfig.JOOBLE_API_KEY,
                    keyword = "Android Kotlin Developer"
                )
            }
        }
    }

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

    fun addSavedSearch(keyword: String, email: String) {
        if (keyword.isBlank() || email.isBlank()) return
        viewModelScope.launch {
            repository.saveSearch(SavedSearchEntity(keyword = keyword.trim(), email = email.trim()))
        }
    }

    fun removeSavedSearch(search: SavedSearchEntity) {
        viewModelScope.launch { repository.deleteSearch(search) }
    }
}

