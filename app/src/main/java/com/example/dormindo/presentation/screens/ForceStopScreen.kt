package com.example.dormindo.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dormindo.domain.entity.TimerStatus
import com.example.dormindo.presentation.viewmodel.TimerUiState
import kotlinx.coroutines.delay
import java.time.Duration
import com.example.dormindo.data.datasource.NotificationDataSource
import org.koin.androidx.compose.get
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.compose.runtime.DisposableEffect
import android.content.Context

@Composable
fun ForceStopScreen(
    timerValue: String,
    uiState: TimerUiState,
    onStartTimer: (Long) -> Unit,
    onStopTimer: () -> Unit,
    onRefreshMedia: () -> Unit,
    onForceStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Timer local separado
    var localTimer by remember { mutableStateOf(0L) } // segundos
    var isRunning by remember { mutableStateOf(false) }
    var isPausedFromService by remember { mutableStateOf(false) }
    var serviceTimerSeconds by remember { mutableStateOf(0L) }
    val notificationDataSource = get<NotificationDataSource>()
    val context = LocalContext.current

    // BroadcastReceiver para receber atualizações do serviço
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == com.example.dormindo.DormindoTimerForegroundService.ACTION_TIMER_UPDATE) {
                    val seconds = intent.getLongExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_TIMER_SECONDS, 0L)
                    val isPaused = intent.getBooleanExtra("isPaused", false)
                    // Atualiza o estado do timer do serviço
                    serviceTimerSeconds = seconds
                    isPausedFromService = isPaused
                    // Log para debug
                    android.util.Log.d("ForceStopScreen", "Recebido broadcast: $seconds segundos, pausado: $isPaused")
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(com.example.dormindo.DormindoTimerForegroundService.ACTION_TIMER_UPDATE), Context.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Efeito para forçar recomposição quando o estado do serviço mudar
    LaunchedEffect(serviceTimerSeconds, isPausedFromService) {
    }

    // Efeito para atualizar o timer local
    LaunchedEffect(isRunning) {
        var wasRunning = false
        while (isRunning && localTimer > 0) {
            wasRunning = true
            delay(1000)
            localTimer--
        }
        if (wasRunning && localTimer == 0L) {
            isRunning = false
            onForceStop() // Só chama se estava rodando e chegou a zero
        }
    }

    // Efeito colateral: ao finalizar o timer global, força parada de mídia
    LaunchedEffect(uiState.timerStatus) {
        if (uiState.timerStatus is TimerStatus.Completed) {
            onForceStop()
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Timer Local (Forçar Parada)",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mostra o timer do serviço se estiver ativo, senão mostra o timer local
        if (serviceTimerSeconds > 0) {
            Text(
                text = String.format("%02d:%02d:%02d", serviceTimerSeconds / 3600, (serviceTimerSeconds % 3600) / 60, serviceTimerSeconds % 60),
                style = MaterialTheme.typography.displayMedium,
                color = if (isPausedFromService) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isPausedFromService) "Timer PAUSADO" else "Timer ATIVO",
                style = MaterialTheme.typography.titleMedium,
                color = if (isPausedFromService) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = String.format("%02d:%02d:%02d", localTimer / 3600, (localTimer % 3600) / 60, localTimer % 60),
                style = MaterialTheme.typography.displayMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controles do timer do serviço (quando ativo)
        if (serviceTimerSeconds > 0) {
            Text(
                text = "Controles do Timer do Serviço",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = if (isPausedFromService) 
                                com.example.dormindo.DormindoTimerForegroundService.ACTION_RESUME 
                            else 
                                com.example.dormindo.DormindoTimerForegroundService.ACTION_PAUSE
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isPausedFromService) "Retomar" else "Pausar")
                }
                
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_CANCEL
                        }
                        context.startService(intent)
                        // Também para o timer local
                        isRunning = false
                        localTimer = 0L
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_ADD_MINUTE
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+1 min")
                }
                
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_ADD_5_MINUTES
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+5 min")
                }
                
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_ADD_10_MINUTES
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+10 min")
                }
                
                Button(
                    onClick = {
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_ADD_15_MINUTES
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+15 min")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Botões de tempo organizados em duas linhas (só mostram quando não há timer do serviço ativo)
        if (serviceTimerSeconds == 0L) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        localTimer = 60 * 1 // 1 minuto
                        isRunning = true
                        // Inicia o serviço foreground
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 60L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("1 min")
                    }
                    Button(onClick = {
                        localTimer = 60 * 2 // 2 minutos
                        isRunning = true
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 120L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("2 min")
                    }
                    Button(onClick = {
                        localTimer = 60 * 5 // 5 minutos
                        isRunning = true
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 300L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("5 min")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        localTimer = 60 * 10 // 10 minutos
                        isRunning = true
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 600L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("10 min")
                    }
                    Button(onClick = {
                        localTimer = 60 * 15 // 15 minutos
                        isRunning = true
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 900L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("15 min")
                    }
                    Button(onClick = {
                        localTimer = 60 * 30 // 30 minutos
                        isRunning = true
                        val intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
                            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_START
                            putExtra(com.example.dormindo.DormindoTimerForegroundService.EXTRA_DURATION, 1800L)
                        }
                        context.startForegroundService(intent)
                    }, enabled = !isRunning) {
                        Text("30 min")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { isRunning = false }, enabled = isRunning) {
                        Text("Pausar")
                    }
                    Button(onClick = { isRunning = true }, enabled = !isRunning && localTimer > 0) {
                        Text("Retomar")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { localTimer += 60 }, enabled = isRunning) {
                    Text("Adicionar 1 min")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { localTimer += 5 * 60 }, enabled = isRunning) {
                    Text("Adicionar 5 min")
                }
            }
        }
        
        // Removido TimerControls e espaçamento relacionado
        Spacer(modifier = Modifier.height(40.dp))
        
        // Botão Force Stop (mantido para funcionalidade específica)
        Button(
            onClick = {
                onForceStop()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        ) {
            Text(
                text = "Force Stop",
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