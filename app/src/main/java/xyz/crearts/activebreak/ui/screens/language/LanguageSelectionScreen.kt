package xyz.crearts.activebreak.ui.screens.language

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import xyz.crearts.activebreak.R
import xyz.crearts.activebreak.data.preferences.SettingsManager
import xyz.crearts.activebreak.ui.navigation.Screen
import xyz.crearts.activebreak.utils.LocaleHelper
import xyz.crearts.activebreak.utils.AppRestartHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    viewModel: LanguageSelectionViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("ru") } // Default to Russian
    var showRestartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.language_selection_title),
                        textAlign = TextAlign.Center
                    ) 
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language icon
            Icon(
                Icons.Default.Language,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title and subtitle
            Text(
                text = stringResource(R.string.language_selection_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.language_selection_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Language options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Russian option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLanguage == "ru",
                                onClick = { selectedLanguage = "ru" }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == "ru",
                            onClick = { selectedLanguage = "ru" }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.language_russian),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // English option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLanguage == "en",
                                onClick = { selectedLanguage = "en" }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == "en",
                            onClick = { selectedLanguage = "en" }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.language_english),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Continue button
            Button(
                onClick = {
                    // Save settings first, then show dialog only if language changed
                    viewModel.setLanguageAndCompleteFirstLaunch(selectedLanguage) { languageChanged ->
                        if (languageChanged) {
                            showRestartDialog = true
                        } else {
                            // Navigate to home without restart dialog
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.LanguageSelection.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.language_continue),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // Restart dialog for language change
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = {
                // Disable dismiss on outside click to prevent accidental closing
                // User must explicitly choose an option
            },
            title = {
                Text(
                    stringResource(R.string.language_change_restart_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    stringResource(R.string.language_change_restart_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        AppRestartHelper.restartApp(context)
                    }
                ) {
                    Text(stringResource(R.string.language_restart_now))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRestartDialog = false
                        // Navigate to home if user chooses later
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.LanguageSelection.route) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.language_restart_later))
                }
            }
        )
    }
}

// ViewModel for LanguageSelectionScreen
class LanguageSelectionViewModel : androidx.lifecycle.ViewModel() {
    private val settingsManager = SettingsManager.instance

    fun setLanguageAndCompleteFirstLaunch(language: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Update language and mark first launch as completed
                val currentSettings = settingsManager.getSettings().first()
                
                // Check if language actually changed
                val languageChanged = currentSettings.language != language
                
                settingsManager.updateSettings(
                    currentSettings.copy(
                        language = language,
                        isFirstLaunch = false
                    )
                )

                // Apply locale change only if language changed
                if (languageChanged) {
                    LocaleHelper.setLocale(language)
                }

                // Add small delay to ensure settings are saved
                kotlinx.coroutines.delay(100)

                onComplete(languageChanged)
            } catch (e: Exception) {
                // If something goes wrong, assume language changed to be safe
                onComplete(true)
            }
        }
    }
}
