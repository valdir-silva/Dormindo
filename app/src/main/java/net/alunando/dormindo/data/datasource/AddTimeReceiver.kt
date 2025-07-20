package net.alunando.dormindo.data.datasource

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import net.alunando.dormindo.R
import net.alunando.dormindo.data.repository.TimerRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddTimeReceiver : BroadcastReceiver(), KoinComponent {
    private val timerRepository: TimerRepositoryImpl by inject()
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "net.alunando.dormindo.ADD_TIME") {
            CoroutineScope(Dispatchers.IO).launch {
                timerRepository.addOneMinuteToTimer()
            }
            Toast.makeText(
                context,
                context.getString(R.string.toast_add_1_min_success),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
} 