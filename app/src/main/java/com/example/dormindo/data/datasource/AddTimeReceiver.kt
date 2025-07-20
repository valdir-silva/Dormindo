package com.example.dormindo.data.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.dormindo.data.repository.TimerRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddTimeReceiver : BroadcastReceiver(), KoinComponent {
    private val timerRepository: TimerRepositoryImpl by inject()
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "com.example.dormindo.ADD_TIME") {
            CoroutineScope(Dispatchers.IO).launch {
                timerRepository.addOneMinuteToTimer()
            }
            Toast.makeText(context, "+1 min adicionado ao timer", Toast.LENGTH_SHORT).show()
        }
    }
} 