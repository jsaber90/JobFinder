package com.ai.jobfinder

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
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
        val repository = JobRepository(AppDatabase.create(applicationContext).jobDao())
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
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Jobs", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::updateQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search jobs, skills, or companies") }
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (job.url.isNotBlank()) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(job.url))
                                    )
                                }
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
            if (jobs.isEmpty()) Text("No saved jobs yet. Background sync will add matches here.")
        }
    }
}

