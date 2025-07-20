package net.alunando.dormindo.presentation.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.alunando.dormindo.DormindoTimerForegroundService
import net.alunando.dormindo.R
import net.alunando.dormindo.presentation.viewmodel.TimerUiState

@Composable
fun ForceStopScreen(
    timerValue: String, // Mantido para compatibilidade, mas não usado diretamente
    uiState: TimerUiState, // Mantido para compatibilidade
    onStartTimer: (Long) -> Unit, // Não usado diretamente, a tela usa intents
    onStopTimer: () -> Unit, // Não usado diretamente
    onRefreshMedia: () -> Unit, // Não usado diretamente
    onForceStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var serviceTimerSeconds by remember { mutableStateOf(0L) }
    var isPausedFromService by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isTimerRunning = serviceTimerSeconds > 0

    // Receiver para atualizações do serviço
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DormindoTimerForegroundService.ACTION_TIMER_UPDATE) {
                    serviceTimerSeconds = intent.getLongExtra(DormindoTimerForegroundService.EXTRA_TIMER_SECONDS, 0L)
                    isPausedFromService = intent.getBooleanExtra("isPaused", false)
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(DormindoTimerForegroundService.ACTION_TIMER_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Solicita uma atualização inicial para sincronizar o estado
    LaunchedEffect(Unit) {
        val intent = Intent(context, DormindoTimerForegroundService::class.java).apply {
            action = DormindoTimerForegroundService.ACTION_REQUEST_UPDATE
        }
        context.startService(intent)
    }

    // Função para iniciar o serviço com uma duração
    val startServiceTimer: (Long) -> Unit = { durationInSeconds ->
        val intent = Intent(context, DormindoTimerForegroundService::class.java).apply {
            action = DormindoTimerForegroundService.ACTION_START
            putExtra(DormindoTimerForegroundService.EXTRA_DURATION, durationInSeconds)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.force_stop_screen_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- EXIBIÇÃO DO TIMER ---
        Text(
            text = String.format("%02d:%02d:%02d", serviceTimerSeconds / 3600, (serviceTimerSeconds % 3600) / 60, serviceTimerSeconds % 60),
            style = MaterialTheme.typography.displayMedium,
            color = if (isTimerRunning && isPausedFromService) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
        if (isTimerRunning) {
            Text(
                text = if (isPausedFromService) stringResource(id = R.string.timer_status_paused) else stringResource(id = R.string.timer_status_active),
                style = MaterialTheme.typography.titleMedium,
                color = if (isPausedFromService) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÕES DE TEMPO PRÉ-DEFINIDOS ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = { startServiceTimer(1 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_1m)) }
                Button(onClick = { startServiceTimer(5 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_5m)) }
                Button(onClick = { startServiceTimer(10 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_10m)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = { startServiceTimer(15 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_15m)) }
                Button(onClick = { startServiceTimer(30 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_30m)) }
                Button(onClick = { startServiceTimer(60 * 60) }, enabled = !isTimerRunning, modifier = Modifier.weight(1f)) { Text(stringResource(id = R.string.timer_preset_60m)) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- CONTROLES DO TIMER (PAUSAR/RETOMAR/CANCELAR) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    val action = if (isPausedFromService) DormindoTimerForegroundService.ACTION_RESUME else DormindoTimerForegroundService.ACTION_PAUSE
                    context.startService(Intent(context, DormindoTimerForegroundService::class.java).setAction(action))
                },
                enabled = isTimerRunning,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isPausedFromService) stringResource(id = R.string.timer_action_resume) else stringResource(id = R.string.timer_action_pause))
            }
            Button(
                onClick = {
                    context.startService(Intent(context, DormindoTimerForegroundService::class.java).setAction(DormindoTimerForegroundService.ACTION_CANCEL))
                },
                enabled = isTimerRunning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.timer_action_cancel))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        // --- BOTÃO DE FORÇAR PARADA ---
        Button(
            onClick = onForceStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        ) {
            Text(
                text = stringResource(id = R.string.force_stop_media_button),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForceStopScreenPreview() {
    ForceStopScreen(
        timerValue = "00:15:23",
        uiState = TimerUiState(),
        onStartTimer = {},
        onStopTimer = {},
        onRefreshMedia = {},
        onForceStop = {}
    )
} 