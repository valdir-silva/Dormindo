package com.example.dormindo

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class DormindoNotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "NotificationListener"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService criado")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService conectado")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Não é necessário implementar nada para apenas obter a permissão
        Log.d(TAG, "Notificação recebida: ${sbn?.packageName}")
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Opcional
        Log.d(TAG, "Notificação removida: ${sbn?.packageName}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationListenerService destruído")
    }
} 