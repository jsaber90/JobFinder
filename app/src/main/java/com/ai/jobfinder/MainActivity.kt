package com.ai.jobfinder

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.jobfinder.data.AppDatabase
import com.ai.jobfinder.data.JobRepository
import com.ai.jobfinder.ui.theme.JobFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.create(applicationContext)
        val repository = JobRepository(database.jobDao(), database.savedSearchDao())
        com.ai.jobfinder.data.JobSyncWorker.schedule(applicationContext)
        setContent {
            JobFinderTheme {
                val jobViewModel: com.ai.jobfinder.ui.JobViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            com.ai.jobfinder.ui.JobViewModel(repository) as T
                    }
                )
                JobFinderScreen(jobViewModel)
            }
        }
    }
}

@Composable
private fun JobFinderScreen(viewModel: com.ai.jobfinder.ui.JobViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Jobs") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Saved searches") })
            }
            if (selectedTab == 0) JobsContent(viewModel) else SavedSearchesContent(viewModel)
        }
    }
}

@Composable
private fun ColumnScope.JobsContent(viewModel: com.ai.jobfinder.ui.JobViewModel) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    OutlinedTextField(
        value = query,
        onValueChange = viewModel::updateQuery,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Search jobs, skills, or companies") }
    )
    LazyColumn(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(jobs, key = { it.id }) { job ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (job.url.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.url)))
                }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(job.title, style = MaterialTheme.typography.titleMedium)
                    Text("${job.company} · ${job.location}")
                    Text(job.source, style = MaterialTheme.typography.labelSmall)
                    Text("Tap to apply", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
    if (jobs.isEmpty()) Text("No matching jobs found yet.")
}

@Composable
private fun ColumnScope.SavedSearchesContent(viewModel: com.ai.jobfinder.ui.JobViewModel) {
    var keyword by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    val searches by viewModel.savedSearches.collectAsState()
    OutlinedTextField(keyword, { keyword = it }, Modifier.fillMaxWidth(), label = { Text("Job keyword") }, singleLine = true)
    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Notification email") }, singleLine = true)
    Button(onClick = { viewModel.addSavedSearch(keyword, email); keyword = "" }) { Text("Save search") }
    LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(searches, key = { it.id }) { search ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(search.keyword, style = MaterialTheme.typography.titleMedium)
                    Text(search.email)
                    Button(onClick = { viewModel.removeSavedSearch(search) }) { Text("Remove") }
                }
            }
        }
    }
}

