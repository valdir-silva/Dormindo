package net.alunando.dormindo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import net.alunando.dormindo.data.datasource.NotificationDataSource
import net.alunando.dormindo.domain.repository.MediaRepository
import net.alunando.dormindo.presentation.screens.ForceStopScreen
import net.alunando.dormindo.presentation.screens.TimerScreen
import net.alunando.dormindo.presentation.viewmodel.TimerViewModel
import net.alunando.dormindo.ui.theme.DormindoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {
    private val mediaRepository: MediaRepository by inject()
    private val notificationDataSource: NotificationDataSource by inject()

    // Launcher para solicitar permissão de notificação
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    // Launcher para abrir configurações de acesso à mídia
    private val requestMediaControlPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Não há resultado direto, o usuário precisa conceder manualmente
            Toast.makeText(
                this,
                getString(R.string.media_permission_check),
                Toast.LENGTH_SHORT
            ).show()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicita permissão de notificação se necessário (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationDataSource.hasNotificationPermission()) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // --- RECEIVER PARA SINCRONIZAR O TEMPO DO SERVIÇO COM A UI ---
        // Removido: não é seguro acessar ViewModel fora de Composable
        // A sincronização será feita dentro do Composable TimerScreen usando LaunchedEffect

        setContent {
            DormindoTheme {
                var selectedTab by remember { mutableStateOf(0) } // 0 = Simples, 1 = Avançado
                var hasNotificationListener by remember {
                    mutableStateOf(
                        hasMediaContentControlPermission()
                    )
                }
                val navController = rememberNavController()

                // Revalida permissão ao retornar para o app
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(300)
                    hasNotificationListener = hasMediaContentControlPermission()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val tabTitles = listOf(stringResource(id = R.string.main_tab_simple_mode), stringResource(id = R.string.main_tab_advanced_mode))
                        val tabIcons = listOf(Icons.Filled.Timer, Icons.Filled.Settings)
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier
                                .height(64.dp)
                                .padding(bottom = 8.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .height(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            divider = {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                val selected = selectedTab == index
                                Tab(
                                    selected = selected,
                                    onClick = {
                                        if (index == 1) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(R.string.in_development),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            selectedTab = index
                                        }
                                    },
                                    modifier = Modifier.fillMaxHeight(),
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Icon(
                                            imageVector = tabIcons[index],
                                            contentDescription = title,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        Text(
                                            text = title,
                                            fontSize = if (selected) 18.sp else 16.sp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        when (selectedTab) {
                            0 -> {
                                val viewModel: TimerViewModel = koinViewModel()
                                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                                val timerValue = uiState.currentTimer?.let { timer ->
                                    val totalSeconds = timer.remainingTime.seconds
                                    val hours = totalSeconds / 3600
                                    val minutes = (totalSeconds % 3600) / 60
                                    val seconds = totalSeconds % 60
                                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                                } ?: stringResource(id = R.string.timer_default_value)
                                ForceStopScreen(
                                    timerValue = timerValue,
                                    uiState = uiState,
                                    onStartTimer = { viewModel.startTimer(it) },
                                    onStopTimer = { viewModel.stopTimer() },
                                    onRefreshMedia = { viewModel.refreshMediaInfo() },
                                    onForceStop = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            mediaRepository.stopMediaPlayback()
                                        }
                                    },
                                    modifier = Modifier,
                                )
                            }

                            1 -> {
                                if (!hasNotificationListener) {
                                    NotificationAccessScreen(onRequestPermission = {
                                        val intent =
                                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                        startActivity(intent)
                                    }, onCheckAgain = {
                                        hasNotificationListener = hasMediaContentControlPermission()
                                    })
                                } else {
                                    TimerScreen(
                                        modifier = Modifier,
                                        onNavigateForceStop = { selectedTab = 0 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Verifica se o app tem permissão de acesso à mídia (Notification Listener)
    private fun hasMediaContentControlPermission(): Boolean {
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        return enabledListeners?.contains(packageName) == true
    }
}

// Tela amigável para solicitar permissão de acesso a notificações
@androidx.compose.runtime.Composable
fun NotificationAccessScreen(
    onRequestPermission: () -> Unit,
    onCheckAgain: () -> Unit
) {
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = stringResource(id = R.string.notification_access_title),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            androidx.compose.material3.Text(
                text = stringResource(id = R.string.notification_access_description),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )
            androidx.compose.material3.Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(stringResource(id = R.string.notification_access_grant_button))
            }
            androidx.compose.material3.Button(
                onClick = onCheckAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                androidx.compose.material3.Text(stringResource(id = R.string.notification_access_check_again_button))
            }
        }
    }
}
