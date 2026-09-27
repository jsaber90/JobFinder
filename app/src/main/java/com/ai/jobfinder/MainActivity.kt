package com.ai.jobfinder

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ai.jobfinder.data.AppDatabase
import com.ai.jobfinder.data.JobRepository
import com.ai.jobfinder.ui.theme.JobFinderTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val preferences by lazy { getSharedPreferences("jobfinder_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage(preferences.getString("language", "en") ?: "en")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.create(applicationContext)
        val repository = JobRepository(database.jobDao(), database.savedSearchDao())
        com.ai.jobfinder.data.JobSyncWorker.schedule(applicationContext)
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
            JobFinderTheme(darkTheme = darkMode) {
                val jobViewModel: com.ai.jobfinder.ui.JobViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            com.ai.jobfinder.ui.JobViewModel(repository) as T
                    }
                )
                JobFinderScreen(
                    viewModel = jobViewModel,
                    darkMode = darkMode,
                    onDarkModeChanged = {
                        darkMode = it
                        preferences.edit().putBoolean("dark_mode", it).apply()
                    },
                    onLanguageChanged = { language ->
                        preferences.edit().putString("language", language).apply()
                        recreate()
                    }
                )
            }
        }
    }

    private fun applyLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun JobFinderScreen(
    viewModel: com.ai.jobfinder.ui.JobViewModel,
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "jobs") {
        composable("jobs") { JobsPage(viewModel, navController) }
        composable("saved") { SavedSearchesPage(viewModel, navController) }
        composable("settings") { SettingsPage(darkMode, onDarkModeChanged, onLanguageChanged, navController) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun JobsPage(viewModel: com.ai.jobfinder.ui.JobViewModel, navController: NavHostController) {
    PageScaffold(
        title = stringResource(R.string.jobs),
        navController = navController,
        showBack = false,
        actions = {
            IconButton(onClick = { navController.navigate("saved") }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = stringResource(R.string.saved_searches_icon_description))
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_icon_description))
            }
        }
    ) { JobsContent(viewModel) }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SavedSearchesPage(viewModel: com.ai.jobfinder.ui.JobViewModel, navController: NavHostController) {
    PageScaffold(stringResource(R.string.saved_searches), navController, true) { SavedSearchesContent(viewModel) }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsPage(darkMode: Boolean, onDarkModeChanged: (Boolean) -> Unit, onLanguageChanged: (String) -> Unit, navController: NavHostController) {
    PageScaffold(stringResource(R.string.settings), navController, true) {
        SettingsContent(darkMode, onDarkModeChanged, onLanguageChanged)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PageScaffold(
    title: String,
    navController: NavHostController,
    showBack: Boolean,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBack) IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = { actions() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun ColumnScope.JobsContent(viewModel: com.ai.jobfinder.ui.JobViewModel) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    OutlinedTextField(query, viewModel::updateQuery, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.search_jobs_hint)) })
    LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(jobs, key = { it.id }) { job ->
            Card(modifier = Modifier.fillMaxWidth().clickable {
                if (job.url.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.url)))
            }) {
                Column(Modifier.padding(16.dp)) {
                    Text(job.title, style = MaterialTheme.typography.titleMedium)
                    Text("${job.company} · ${job.location}")
                    Text(job.source, style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.tap_to_apply), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
    if (jobs.isEmpty()) Text(stringResource(R.string.no_matching_jobs))
}

@Composable
private fun ColumnScope.SavedSearchesContent(viewModel: com.ai.jobfinder.ui.JobViewModel) {
    var keyword by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    val searches by viewModel.savedSearches.collectAsState()
    OutlinedTextField(keyword, { keyword = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.job_keyword)) }, singleLine = true)
    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.notification_email)) }, singleLine = true)
    Button(onClick = { viewModel.addSavedSearch(keyword, email); keyword = "" }) { Text(stringResource(R.string.save_search)) }
    LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(searches, key = { it.id }) { search ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(search.keyword, style = MaterialTheme.typography.titleMedium)
                    Text(search.email)
                    Button(onClick = { viewModel.removeSavedSearch(search) }) { Text(stringResource(R.string.remove)) }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(darkMode: Boolean, onDarkModeChanged: (Boolean) -> Unit, onLanguageChanged: (String) -> Unit) {
    Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.dark_mode), modifier = Modifier.weight(1f))
        Switch(checked = darkMode, onCheckedChange = onDarkModeChanged)
    }
    Text(stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onLanguageChanged("en") }) { Text(stringResource(R.string.english)) }
        TextButton(onClick = { onLanguageChanged("ar") }) { Text(stringResource(R.string.arabic)) }
    }
    Text(stringResource(R.string.email_delivery_note), style = MaterialTheme.typography.bodySmall)
}

