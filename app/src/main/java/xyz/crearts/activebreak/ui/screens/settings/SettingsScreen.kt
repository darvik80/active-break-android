package xyz.crearts.activebreak.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Активное время",
                        style = MaterialTheme.typography.titleMedium
                    )

                    var showStartTimePicker by remember { mutableStateOf(false) }
                    var showEndTimePicker by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Начало:")
                            Text(
                                "${settings.startHour}:${String.format("%02d", settings.startMinute)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Конец:")
                            Text(
                                "${settings.endHour}:${String.format("%02d", settings.endMinute)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }

                    if (showStartTimePicker) {
                        xyz.crearts.activebreak.ui.screens.todo.TimePickerDialog(
                            onDismiss = { showStartTimePicker = false },
                            onConfirm = { hour, minute ->
                                viewModel.updateStartTime(hour, minute)
                                showStartTimePicker = false
                            },
                            initialHour = settings.startHour,
                            initialMinute = settings.startMinute
                        )
                    }

                    if (showEndTimePicker) {
                        xyz.crearts.activebreak.ui.screens.todo.TimePickerDialog(
                            onDismiss = { showEndTimePicker = false },
                            onConfirm = { hour, minute ->
                                viewModel.updateEndTime(hour, minute)
                                showEndTimePicker = false
                            },
                            initialHour = settings.endHour,
                            initialMinute = settings.endMinute
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Интервал напоминаний",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "${settings.intervalMinutes} минут",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = settings.intervalMinutes.toFloat(),
                        onValueChange = { viewModel.updateInterval(it.toLong()) },
                        valueRange = 15f..120f,
                        steps = 20
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("15 мин", style = MaterialTheme.typography.bodySmall)
                        Text("120 мин", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Уведомления",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Перерывы")
                        Switch(
                            checked = settings.breakNotificationsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(breakNotificationsEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Задачи (TODO)")
                        Switch(
                            checked = settings.todoNotificationsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(todoNotificationsEnabled = it)) }
                        )
                    }
                }
            }

            val tips = remember {
                listOf(
                    "Рекомендуется делать перерыв каждые 30-45 минут для поддержания здоровья и продуктивности.",
                    "Пейте воду регулярно, даже если не чувствуете жажды. Это улучшает работу мозга 🧠.",
                    "Проветривайте помещение каждые 2-3 часа. Свежий воздух бодрит! 🌬️",
                    "Правило 20-20-20: каждые 20 минут смотрите на 20 футов (6 метров) вдаль в течение 20 секунд 👀.",
                    "Короткая прогулка помогает освежить мысли и улучшить концентрацию 🚶.",
                    "Следите за осанкой: держите спину ровно, а монитор на уровне глаз 🪑.",
                    "Не забывайте моргать, глядя в экран, чтобы избежать сухости глаз 👁️.",
                    "Небольшая разминка шеи поможет избежать головной боли 🧘‍♂️."
                )
            }
            val currentTip = remember { tips.random() }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "💡 Совет",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentTip,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Интеграции с мессенджерами
            IntegrationsCard(
                settings = settings,
                viewModel = viewModel
            )

            // Тестирование уведомлений
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "🧪 Тестирование",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Проверьте работу уведомлений",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    OutlinedButton(
                        onClick = { viewModel.testBreakNotification() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Тест уведомления о перерыве")
                    }

                    OutlinedButton(
                        onClick = { viewModel.testTodoNotification() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Тест уведомления TODO")
                    }
                }
            }
            
            // Дополнительный отступ внизу для удобства прокрутки
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun IntegrationsCard(
    settings: xyz.crearts.activebreak.data.preferences.Settings,
    viewModel: SettingsViewModel
) {
    var showTelegramDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Интеграции с мессенджерами",
                style = MaterialTheme.typography.titleMedium
            )

            // Telegram
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Telegram Bot", style = MaterialTheme.typography.bodyLarge)
                    if (settings.telegramEnabled) {
                        Text(
                            "Настроен ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row {
                    Switch(
                        checked = settings.telegramEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked && (settings.telegramBotToken.isBlank() || settings.telegramChatId.isBlank())) {
                                showTelegramDialog = true
                            } else {
                                viewModel.updateSettings(settings.copy(telegramEnabled = isChecked))
                            }
                        }
                    )
                    IconButton(onClick = { showTelegramDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настроить")
                    }
                }
            }

            // Telegram Dialog
            if (showTelegramDialog) {
                TelegramSetupDialog(
                    currentToken = settings.telegramBotToken,
                    currentChatId = settings.telegramChatId,
                    onDismiss = { showTelegramDialog = false },
                    onSave = { token, chatId ->
                        viewModel.updateSettings(
                            settings.copy(
                                telegramEnabled = true,
                                telegramBotToken = token,
                                telegramChatId = chatId
                            )
                        )
                        showTelegramDialog = false
                    },
                    onTest = { token, chatId ->
                        viewModel.testTelegramIntegration(token, chatId)
                    }
                )
            }

        }
    }
}

@Composable
fun TelegramSetupDialog(
    currentToken: String,
    currentChatId: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onTest: (String, String) -> Unit
) {
    var token by remember { mutableStateOf(currentToken) }
    var chatId by remember { mutableStateOf(currentChatId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка Telegram Bot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Инструкция:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    "1. Создайте бота через @BotFather\n" +
                    "2. Скопируйте токен бота\n" +
                    "3. Напишите боту /start\n" +
                    "4. Получите chat_id через @userinfobot",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Bot Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = chatId,
                    onValueChange = { chatId = it },
                    label = { Text("Chat ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedButton(
                    onClick = { onTest(token, chatId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = token.isNotBlank() && chatId.isNotBlank()
                ) {
                    Text("Тест отправки")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(token, chatId) },
                enabled = token.isNotBlank() && chatId.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}