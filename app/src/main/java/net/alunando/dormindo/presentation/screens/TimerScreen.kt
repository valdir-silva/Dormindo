package net.alunando.dormindo.presentation.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.alunando.dormindo.R
import net.alunando.dormindo.DormindoTimerForegroundService
import net.alunando.dormindo.domain.entity.TimerStatus
import net.alunando.dormindo.presentation.viewmodel.TimerUiState
import net.alunando.dormindo.presentation.viewmodel.TimerViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Tela principal do timer
 */
@Composable
fun TimerScreen(
    modifier: Modifier,
    viewModel: TimerViewModel = koinViewModel(),
    onNavigateForceStop: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // BroadcastReceiver para receber atualizações do serviço
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DormindoTimerForegroundService.ACTION_TIMER_UPDATE) {
                    val seconds = intent.getLongExtra(DormindoTimerForegroundService.EXTRA_TIMER_SECONDS, 0L)
                    val isPaused = intent.getBooleanExtra("isPaused", false)
                    viewModel.updatePauseStateFromService(seconds, isPaused)
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(DormindoTimerForegroundService.ACTION_TIMER_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = stringResource(id = R.string.timer_screen_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Informações da mídia atual
        uiState.currentMediaInfo?.let { mediaInfo ->
            MediaInfoCard(mediaInfo = mediaInfo)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Status do timer
        TimerStatusCard(uiState.timerStatus)

        // Exibe contagem regressiva em segundos
        if (uiState.timerStatus is TimerStatus.Running || uiState.timerStatus is TimerStatus.Paused) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatSeconds(uiState.remainingSeconds),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controles do timer
        TimerControls(
            uiState = uiState,
            onStartTimer = { duration ->
                viewModel.startTimer(duration)
            },
            onStopTimer = {
                viewModel.stopTimer()
            },
            onRefreshMedia = {
                viewModel.refreshMediaInfo()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading e erro
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateForceStop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(id = R.string.force_stop_button), color = MaterialTheme.colorScheme.onError)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de teste para broadcast
        Button(
            onClick = {
                val intent = Intent(DormindoTimerForegroundService.ACTION_TIMER_UPDATE).apply {
                    setPackage(context.packageName)
                }
                intent.putExtra(DormindoTimerForegroundService.EXTRA_TIMER_SECONDS, 999L)
                intent.putExtra("isPaused", true)
                context.sendBroadcast(intent)
                android.widget.Toast.makeText(context, context.getString(R.string.test_broadcast_toast), android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(stringResource(id = R.string.test_broadcast_button), color = MaterialTheme.colorScheme.onTertiary)
        }
    }
}

@Composable
private fun MediaInfoCard(mediaInfo: net.alunando.dormindo.domain.repository.MediaInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = if (mediaInfo.isPlaying) painterResource(id = R.drawable.ic_play) else painterResource(id = R.drawable.ic_pause),
                    contentDescription = if (mediaInfo.isPlaying) stringResource(id = R.string.media_info_card_playing_description) else stringResource(id = R.string.media_info_card_paused_description)
                )
                Text(
                    text = stringResource(id = R.string.media_info_card_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mediaInfo.appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            mediaInfo.title?.let { title ->
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            mediaInfo.artist?.let { artist ->
                Text(
                    text = artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimerStatusCard(timerStatus: TimerStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val iconRes = when (timerStatus) {
                    is TimerStatus.Running -> R.drawable.ic_play
                    is TimerStatus.Paused -> R.drawable.ic_pause
                    is TimerStatus.Completed -> R.drawable.ic_check
                    else -> R.drawable.ic_timer
                }
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(id = R.string.timer_status_card_description)
                )
                Text(
                    text = stringResource(id = R.string.timer_status_card_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (timerStatus) {
                    is TimerStatus.Idle -> stringResource(id = R.string.timer_status_idle)
                    is TimerStatus.Running -> stringResource(id = R.string.timer_status_running)
                    is TimerStatus.Paused -> stringResource(id = R.string.timer_status_paused_details)
                    is TimerStatus.Completed -> stringResource(id = R.string.timer_status_completed)
                    is TimerStatus.Error -> stringResource(id = R.string.timer_status_error, timerStatus.message)
                },
                fontSize = 16.sp,
                color = when (timerStatus) {
                    is TimerStatus.Running -> MaterialTheme.colorScheme.primary
                    is TimerStatus.Completed -> MaterialTheme.colorScheme.secondary
                    is TimerStatus.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
fun TimerControls(
    uiState: TimerUiState,
    onStartTimer: (Long) -> Unit,
    onStopTimer: () -> Unit,
    onRefreshMedia: () -> Unit
) {
    var selectedDuration by remember { mutableStateOf(30L) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seletor de duração
        Text(
            text = stringResource(id = R.string.timer_duration_selector_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(15L, 30L, 45L, 60L).forEach { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { selectedDuration = duration },
                    label = { Text(stringResource(id = R.string.timer_duration_minutes, duration)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botões de controle
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onStartTimer(selectedDuration) },
                enabled = uiState.timerStatus !is TimerStatus.Running && !uiState.isLoading
            ) {
                Text(stringResource(id = R.string.timer_action_start))
            }

            Button(
                onClick = onStopTimer,
                enabled = uiState.timerStatus is TimerStatus.Running && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(id = R.string.timer_action_stop))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para atualizar informações da mídia
        Button(
            onClick = onRefreshMedia,
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(stringResource(id = R.string.timer_action_refresh_media))
        }
    }
}

// Função utilitária para formatar segundos em mm:ss
fun formatSeconds(seconds: Long): String {
    val min = seconds / 60
    val sec = seconds % 60
    return String.format("%02d:%02d", min, sec)
}
