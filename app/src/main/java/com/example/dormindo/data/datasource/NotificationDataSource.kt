package com.example.dormindo.data.datasource

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dormindo.MainActivity
import com.example.dormindo.R

/**
 * DataSource para gerenciar notificações do timer
 */
class NotificationDataSource(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var lastNotificationSeconds: Long = -1L
    private var lastNotificationPaused: Boolean = false

    companion object {
        private const val CHANNEL_ID = "dormindo_timer_channel"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_NAME = "Timer do Dormindo"
        private const val CHANNEL_DESCRIPTION = "Notificações do timer para parar reprodução de mídia"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Cria o canal de notificação (necessário para Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Alta prioridade, mas ainda silencioso
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null) // Sem som
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Constrói a notificação do timer com segundos e botões de ação
     */
    fun buildTimerNotification(remainingSeconds: Long, isPaused: Boolean): Notification {
        // Não faz reset aqui - deixa o controle para o updateTimerNotification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ações
        val pauseIntent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            context, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val resumeIntent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_RESUME
        }
        val resumePendingIntent = PendingIntent.getService(
            context, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cancelIntent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            context, 3, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Botão +15 min
        val add15Intent = Intent(context, com.example.dormindo.DormindoTimerForegroundService::class.java).apply {
            action = com.example.dormindo.DormindoTimerForegroundService.ACTION_ADD_15_MINUTES
        }
        val add15PendingIntent = PendingIntent.getService(
            context, 15, add15Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Timer Ativo")
            .setContentText("Restam ${formatSeconds(remainingSeconds)}")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Máxima prioridade, mas sem barulho
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true) // Não faz barulho
            .setVibrate(null) // Não vibra
            .setSound(null) // Não faz som
            .addAction(
                if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                if (isPaused) "Retomar" else "Pausar",
                if (isPaused) resumePendingIntent else pausePendingIntent
            )
            .addAction(R.drawable.ic_check, "Cancelar", cancelPendingIntent)
            .addAction(R.drawable.ic_timer, "+15 min", add15PendingIntent)

        return builder.build()
    }

    /**
     * Atualiza a notificação do timer com segundos e estado de pausa
     */
    fun updateTimerNotification(remainingSeconds: Long, isPaused: Boolean) {
        // Só atualiza se os valores mudaram para evitar spam
        if (lastNotificationSeconds != remainingSeconds || lastNotificationPaused != isPaused) {
            val notification = buildTimerNotification(remainingSeconds, isPaused)
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            // Atualiza os valores do último broadcast
            lastNotificationSeconds = remainingSeconds
            lastNotificationPaused = isPaused
            
            // Log para debug (reduzido para não spam)
            if (remainingSeconds % 10 == 0L) {
                android.util.Log.d("NotificationDataSource", "Notificação atualizada: $remainingSeconds segundos, pausado: $isPaused")
            }
        }
    }

    /**
     * Mostra notificação de timer concluído
     */
    fun showTimerCompletedNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Timer Concluído")
            .setContentText("Reprodução de mídia foi parada")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Máxima prioridade, mas sem barulho
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true) // Sem som na conclusão também
            .setVibrate(null) // Não vibra
            .setSound(null) // Não faz som
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancela a notificação do timer
     */
    fun cancelTimerNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        // Reset dos valores de controle
        lastNotificationSeconds = -1L
        lastNotificationPaused = false
        android.util.Log.d("NotificationDataSource", "Notificação cancelada")
    }

    /**
     * Verifica se a notificação está ativa
     */
    fun isNotificationActive(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
        } else {
            false
        }
    }

    /**
     * Verifica se a permissão de notificação está concedida (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Em versões anteriores não é necessário
        }
    }

    private fun formatSeconds(seconds: Long): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", min, sec)
    }
}