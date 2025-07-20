package net.alunando.dormindo

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.alunando.dormindo.data.datasource.NotificationDataSource
import java.time.Duration

class DormindoTimerForegroundService : Service() {
    companion object {
        const val ACTION_START = "net.alunando.dormindo.action.START_TIMER"
        const val ACTION_PAUSE = "net.alunando.dormindo.action.PAUSE_TIMER"
        const val ACTION_RESUME = "net.alunando.dormindo.action.RESUME_TIMER"
        const val ACTION_CANCEL = "net.alunando.dormindo.action.CANCEL_TIMER"
        const val ACTION_ADD_MINUTE = "net.alunando.dormindo.action.ADD_MINUTE"
        const val ACTION_ADD_5_MINUTES = "net.alunando.dormindo.action.ADD_5_MINUTES"
        const val ACTION_ADD_10_MINUTES = "net.alunando.dormindo.action.ADD_10_MINUTES"
        const val ACTION_ADD_15_MINUTES = "net.alunando.dormindo.action.ADD_15_MINUTES"
        const val ACTION_REQUEST_UPDATE = "net.alunando.dormindo.action.REQUEST_UPDATE"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_REMAINING = "extra_remaining"
        const val TIMER_NOTIFICATION_ID = 1001 // Usar o mesmo ID do NotificationDataSource
        const val ACTION_TIMER_UPDATE = "net.alunando.dormindo.action.TIMER_UPDATE"
        const val EXTRA_TIMER_SECONDS = "extra_timer_seconds"
    }

    private var timerJob: Job? = null
    private var remainingSeconds: Long = 0L
    private var totalSeconds: Long = 0L
    private var isPaused: Boolean = false
    private var lastBroadcastSeconds: Long = -1L
    private var lastBroadcastPaused: Boolean = false
    private lateinit var notificationDataSource: NotificationDataSource
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        notificationDataSource = NotificationDataSource(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                startTimer(duration)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_CANCEL -> cancelTimer()
            ACTION_ADD_MINUTE -> addMinutes(1)
            ACTION_ADD_5_MINUTES -> addMinutes(5)
            ACTION_ADD_10_MINUTES -> addMinutes(10)
            ACTION_ADD_15_MINUTES -> addMinutes(15)
            ACTION_REQUEST_UPDATE -> sendTimerUpdateBroadcast()
        }
        return START_STICKY
    }

    private fun sendTimerUpdateBroadcast() {
        // Só envia broadcast se os valores mudaram para evitar spam
        if (lastBroadcastSeconds != remainingSeconds || lastBroadcastPaused != isPaused) {
            val intent = Intent(ACTION_TIMER_UPDATE).apply {
                setPackage(packageName)
            }
            intent.putExtra(EXTRA_TIMER_SECONDS, remainingSeconds)
            intent.putExtra("isPaused", isPaused)
            sendBroadcast(intent)

            // Atualiza os valores do último broadcast
            lastBroadcastSeconds = remainingSeconds
            lastBroadcastPaused = isPaused

            // Log para debug
            android.util.Log.d("TimerService", "Broadcast enviado: $remainingSeconds segundos, pausado: $isPaused")
        }
    }

    private fun startTimer(durationSeconds: Long) {
        timerJob?.cancel()
        remainingSeconds = durationSeconds
        totalSeconds = durationSeconds
        isPaused = false
        // Reset dos valores de controle do broadcast
        lastBroadcastSeconds = -1L
        lastBroadcastPaused = false
        startForeground(
            TIMER_NOTIFICATION_ID,
            notificationDataSource.buildTimerNotification(remainingSeconds, isPaused)
        )
        // Força primeira atualização da notificação
        notificationDataSource.updateTimerNotification(remainingSeconds, isPaused)
        sendTimerUpdateBroadcast()
        timerJob = serviceScope.launch {
            while (remainingSeconds > 0 && isActive) {
                if (!isPaused) {
                    delay(1000)
                    remainingSeconds--
                    // Envia broadcast a cada segundo para atualizar a interface em tempo real
                    sendTimerUpdateBroadcast()
                    // Atualiza notificação a cada 5 segundos para reduzir spam
                    if (remainingSeconds % 5 == 0L || remainingSeconds <= 10) {
                        notificationDataSource.updateTimerNotification(remainingSeconds, isPaused)
                    }
                } else {
                    delay(500)
                }
            }
            if (remainingSeconds <= 0) {
                notificationDataSource.showTimerCompletedNotification()
                sendTimerUpdateBroadcast() // Só envia quando termina
                stopSelf()
            }
        }
    }

    private fun pauseTimer() {
        isPaused = true
        notificationDataSource.updateTimerNotification(remainingSeconds, isPaused)
        sendTimerUpdateBroadcast() // Só envia quando pausa/retoma
    }

    private fun resumeTimer() {
        isPaused = false
        notificationDataSource.updateTimerNotification(remainingSeconds, isPaused)
        sendTimerUpdateBroadcast() // Só envia quando pausa/retoma
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        remainingSeconds = 0L
        isPaused = false
        // Reset dos valores de controle do broadcast
        lastBroadcastSeconds = -1L
        lastBroadcastPaused = false
        notificationDataSource.cancelTimerNotification()
        sendTimerUpdateBroadcast()
        stopSelf()
    }

    private fun addMinutes(min: Int) {
        remainingSeconds += min * 60
        notificationDataSource.updateTimerNotification(remainingSeconds, isPaused)
        sendTimerUpdateBroadcast() // Só envia quando adiciona tempo
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
 